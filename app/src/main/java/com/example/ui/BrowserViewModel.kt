package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BookmarkEntry
import com.example.data.BrowserRepository
import com.example.data.DailyBlockSummary
import com.example.data.DomainBlockSummary
import com.example.data.HistoryEntry
import com.example.data.SecurityAlert
import com.example.data.WhitelistedSite
import com.example.engine.AdBlockerEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class NavigationTab {
    BROWSER,
    ANALYTICS,
    WHITELIST,
    HISTORY,
    ALERTS,
    DOWNLOADS,
    API_DOCS
}

enum class SearchEngine(val displayName: String, val searchUrl: String) {
    GOOGLE("Google", "https://www.google.com/search?q="),
    DUCKDUCKGO("DuckDuckGo", "https://duckduckgo.com/?q="),
    BING("Bing", "https://www.bing.com/search?q="),
    BRAVE("Brave Search", "https://search.brave.com/search?q="),
    YAHOO("Yahoo", "https://search.yahoo.com/search?q=")
}

data class TabItem(
    val id: String = UUID.randomUUID().toString(),
    val url: String = "https://www.google.com",
    val urlInput: String = "https://www.google.com",
    val title: String = "Google",
    val progress: Int = 100,
    val isLoading: Boolean = false,
    val isIncognito: Boolean = false,
    val isDesktopMode: Boolean = false,
    val isReaderMode: Boolean = false,
    val pageBlockedCount: Int = 0,
    val isWhitelisted: Boolean = false,
    val isBookmarked: Boolean = false,
    val loadStartTimestamp: Long = 0L,
    val lastRenderDurationMs: Long = 0L
)

data class DownloadItem(
    val id: Long = System.currentTimeMillis(),
    val fileName: String,
    val url: String,
    val mimeType: String,
    val fileSize: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Selesai"
)

data class BrowserUiState(
    val tabs: List<TabItem> = listOf(TabItem()),
    val activeTabId: String = tabs.first().id,
    val searchEngine: SearchEngine = SearchEngine.GOOGLE,
    val isAdBlockEnabled: Boolean = true,
    val isDataSaverEnabled: Boolean = false,
    val downloads: List<DownloadItem> = emptyList(),
    val selectedTab: NavigationTab = NavigationTab.BROWSER,
    val isTabSwitcherOpen: Boolean = false,
    val isDownloadsSheetOpen: Boolean = false,
    val isSearchEngineDialogOpen: Boolean = false,
    val isFindInPageOpen: Boolean = false,
    val findQuery: String = "",
    val findMatchIndex: Int = 0,
    val findMatchCount: Int = 0,
    val textZoomPercent: Int = 100,
    val isTurboModeEnabled: Boolean = true,
    val isAutoTabHibernationEnabled: Boolean = true,
    val isZoomDialogOpen: Boolean = false,
    val isClearDataDialogOpen: Boolean = false,
    val isQrDialogOpen: Boolean = false,
    val snackbarMessage: String? = null
) {
    val activeTab: TabItem
        get() = tabs.find { it.id == activeTabId } ?: tabs.firstOrNull() ?: TabItem()

    // Backward compatibility delegates
    val currentUrl: String get() = activeTab.url
    val urlInput: String get() = activeTab.urlInput
    val pageTitle: String get() = activeTab.title
    val pageProgress: Int get() = activeTab.progress
    val isLoading: Boolean get() = activeTab.isLoading
    val isIncognito: Boolean get() = activeTab.isIncognito
    val isDesktopMode: Boolean get() = activeTab.isDesktopMode
    val isReaderMode: Boolean get() = activeTab.isReaderMode
    val pageBlockedCount: Int get() = activeTab.pageBlockedCount
    val isCurrentPageWhitelisted: Boolean get() = activeTab.isWhitelisted
    val isCurrentBookmarked: Boolean get() = activeTab.isBookmarked
}

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BrowserRepository(AppDatabase.getDatabase(application).browserDao())

    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    // Reactive database flows
    val whitelistedSites: StateFlow<List<WhitelistedSite>> = repository.whitelistedSites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayBlockedCount: StateFlow<Int> = repository.getTodayBlockedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalBlockedCount: StateFlow<Int> = repository.totalBlockedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val monthlyBlockedCount: StateFlow<Int> = repository.getCurrentMonthBlockedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val dailySummaries: StateFlow<List<DailyBlockSummary>> = repository.dailySummaries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topBlockedDomains: StateFlow<List<DomainBlockSummary>> = repository.topBlockedDomains
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historyEntries: StateFlow<List<HistoryEntry>> = repository.historyEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkEntries: StateFlow<List<BookmarkEntry>> = repository.bookmarkEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val securityAlerts: StateFlow<List<SecurityAlert>> = repository.securityAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Observe whitelist changes to update current active site whitelist status
        viewModelScope.launch {
            whitelistedSites.collect { sites ->
                val currentHost = AdBlockerEngine.extractHost(_uiState.value.currentUrl)
                val whitelistSet = sites.map { it.domain }.toSet()
                val isWhitelisted = AdBlockerEngine.isWhitelisted(currentHost, whitelistSet)
                updateActiveTab { it.copy(isWhitelisted = isWhitelisted) }
            }
        }
    }

    private fun updateActiveTab(transform: (TabItem) -> TabItem) {
        _uiState.value = _uiState.value.copy(
            tabs = _uiState.value.tabs.map { tab ->
                if (tab.id == _uiState.value.activeTabId) transform(tab) else tab
            }
        )
    }

    // --- Tab Switcher & Management ---
    fun openTabSwitcher(open: Boolean) {
        _uiState.value = _uiState.value.copy(isTabSwitcherOpen = open)
    }

    fun addNewTab(isIncognito: Boolean = false) {
        val defaultUrl = if (isIncognito) "https://www.google.com" else _uiState.value.searchEngine.searchUrl
        val newTab = TabItem(
            url = defaultUrl,
            urlInput = defaultUrl,
            title = if (isIncognito) "Tab Penyamaran" else "Tab Baru",
            isIncognito = isIncognito
        )
        _uiState.value = _uiState.value.copy(
            tabs = _uiState.value.tabs + newTab,
            activeTabId = newTab.id,
            isTabSwitcherOpen = false,
            selectedTab = NavigationTab.BROWSER
        )
        checkCurrentPageWhitelistStatus(defaultUrl)
    }

    fun selectActiveTab(tabId: String) {
        _uiState.value = _uiState.value.copy(
            activeTabId = tabId,
            isTabSwitcherOpen = false,
            selectedTab = NavigationTab.BROWSER
        )
        val selected = _uiState.value.tabs.find { it.id == tabId }
        if (selected != null) {
            checkCurrentPageWhitelistStatus(selected.url)
        }
    }

    fun closeTab(tabId: String) {
        val currentTabs = _uiState.value.tabs
        if (currentTabs.size <= 1) {
            val fresh = TabItem(url = "https://www.google.com", urlInput = "https://www.google.com", title = "Google")
            _uiState.value = _uiState.value.copy(
                tabs = listOf(fresh),
                activeTabId = fresh.id
            )
            return
        }

        val updatedTabs = currentTabs.filter { it.id != tabId }
        val newActiveId = if (_uiState.value.activeTabId == tabId) {
            updatedTabs.last().id
        } else {
            _uiState.value.activeTabId
        }

        _uiState.value = _uiState.value.copy(
            tabs = updatedTabs,
            activeTabId = newActiveId
        )
    }

    fun closeAllTabs() {
        val fresh = TabItem(url = "https://www.google.com", urlInput = "https://www.google.com", title = "Google")
        _uiState.value = _uiState.value.copy(
            tabs = listOf(fresh),
            activeTabId = fresh.id,
            isTabSwitcherOpen = false
        )
    }

    // --- Search Engine Selection ---
    fun openSearchEngineDialog(open: Boolean) {
        _uiState.value = _uiState.value.copy(isSearchEngineDialogOpen = open)
    }

    fun setSearchEngine(engine: SearchEngine) {
        _uiState.value = _uiState.value.copy(
            searchEngine = engine,
            isSearchEngineDialogOpen = false,
            snackbarMessage = "Mesin pencari diubah ke ${engine.displayName}"
        )
    }

    // --- Page Features: Desktop Site, Reader Mode, Incognito Mode ---
    fun toggleDesktopModeForActiveTab() {
        val newMode = !_uiState.value.activeTab.isDesktopMode
        updateActiveTab { it.copy(isDesktopMode = newMode) }
        _uiState.value = _uiState.value.copy(
            snackbarMessage = if (newMode) "Mode Situs Desktop diaktifkan" else "Mode Seluler diaktifkan"
        )
    }

    fun toggleIncognitoForActiveTab() {
        val newMode = !_uiState.value.activeTab.isIncognito
        updateActiveTab { it.copy(isIncognito = newMode) }
        _uiState.value = _uiState.value.copy(
            snackbarMessage = if (newMode) "Mode Penyamaran (Incognito) aktif" else "Mode Penyamaran nonaktif"
        )
    }

    fun toggleReaderModeForActiveTab() {
        val newMode = !_uiState.value.activeTab.isReaderMode
        updateActiveTab { it.copy(isReaderMode = newMode) }
        _uiState.value = _uiState.value.copy(
            snackbarMessage = if (newMode) "Mode Baca (Reader Mode) aktif" else "Mode Normal aktif"
        )
    }

    fun toggleDataSaverMode() {
        val newMode = !_uiState.value.isDataSaverEnabled
        _uiState.value = _uiState.value.copy(
            isDataSaverEnabled = newMode,
            snackbarMessage = if (newMode) "Penghemat Kuota (Data Saver) Aktif • Pemuatan gambar dinonaktifkan" else "Penghemat Kuota Nonaktif"
        )
    }

    fun toggleTurboMode() {
        val newMode = !_uiState.value.isTurboModeEnabled
        _uiState.value = _uiState.value.copy(
            isTurboModeEnabled = newMode,
            snackbarMessage = if (newMode) "Akselerasi Turbo GPU & WebCache Aktif • Halaman dimuat lebih cepat" else "Akselerasi Turbo Nonaktif"
        )
    }

    fun toggleAutoTabHibernation() {
        val newMode = !_uiState.value.isAutoTabHibernationEnabled
        _uiState.value = _uiState.value.copy(
            isAutoTabHibernationEnabled = newMode,
            snackbarMessage = if (newMode) "Hibernasi Tab Pasif Aktif • Hemat Memori RAM & Baterai" else "Hibernasi Tab Pasif Nonaktif"
        )
    }

    // --- Downloads Manager ---
    fun addDownload(fileName: String, url: String, mimeType: String, fileSize: String) {
        val newDownload = DownloadItem(
            fileName = fileName,
            url = url,
            mimeType = mimeType,
            fileSize = fileSize,
            status = "Selesai"
        )
        _uiState.value = _uiState.value.copy(
            downloads = listOf(newDownload) + _uiState.value.downloads,
            snackbarMessage = "Mengunduh $fileName..."
        )
    }

    fun removeDownload(id: Long) {
        _uiState.value = _uiState.value.copy(
            downloads = _uiState.value.downloads.filter { it.id != id }
        )
    }

    fun clearDownloads() {
        _uiState.value = _uiState.value.copy(
            downloads = emptyList(),
            snackbarMessage = "Daftar unduhan dibersihkan"
        )
    }

    // --- Navigation & Web Loading ---
    fun selectTab(tab: NavigationTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun onUrlInputChanged(input: String) {
        updateActiveTab { it.copy(urlInput = input) }
    }

    fun loadUrl(inputUrl: String) {
        var formatted = inputUrl.trim()
        if (formatted.isEmpty()) return

        if (!formatted.startsWith("http://") && !formatted.startsWith("https://")) {
            formatted = if (formatted.contains(".") && !formatted.contains(" ")) {
                "https://$formatted"
            } else {
                "${_uiState.value.searchEngine.searchUrl}${java.net.URLEncoder.encode(formatted, "UTF-8")}"
            }
        }

        updateActiveTab { tab ->
            tab.copy(
                url = formatted,
                urlInput = formatted,
                pageBlockedCount = 0,
                isLoading = true
            )
        }
        _uiState.value = _uiState.value.copy(selectedTab = NavigationTab.BROWSER)
        checkCurrentPageWhitelistStatus(formatted)
    }

    fun onPageStarted(url: String) {
        val host = AdBlockerEngine.extractHost(url)
        val whitelistSet = whitelistedSites.value.map { it.domain }.toSet()
        val isWhitelisted = AdBlockerEngine.isWhitelisted(host, whitelistSet)
        val startMs = System.currentTimeMillis()

        updateActiveTab { tab ->
            tab.copy(
                url = url,
                urlInput = url,
                pageBlockedCount = 0,
                isLoading = true,
                isWhitelisted = isWhitelisted,
                loadStartTimestamp = startMs
            )
        }
    }

    fun onPageFinished(url: String, title: String?) {
        val finalTitle = if (!title.isNullOrEmpty() && title != "about:blank") title else url
        val currentTab = _uiState.value.activeTab
        val now = System.currentTimeMillis()
        val durationMs = if (currentTab.loadStartTimestamp > 0) {
            (now - currentTab.loadStartTimestamp).coerceIn(80L, 5000L)
        } else 120L

        updateActiveTab { tab ->
            tab.copy(
                url = url,
                urlInput = url,
                title = finalTitle,
                isLoading = false,
                progress = 100,
                lastRenderDurationMs = durationMs
            )
        }

        // Record history ONLY if NOT in Incognito Mode
        if (!currentTab.isIncognito) {
            viewModelScope.launch {
                repository.addHistory(finalTitle, url)
            }
        }
    }

    fun onPageProgress(progress: Int) {
        updateActiveTab { tab ->
            tab.copy(
                progress = progress,
                isLoading = progress < 100
            )
        }
    }

    fun toggleAdBlockerGlobal() {
        val newState = !_uiState.value.isAdBlockEnabled
        _uiState.value = _uiState.value.copy(
            isAdBlockEnabled = newState,
            snackbarMessage = if (newState) "Pemblokir iklan diaktifkan" else "Pemblokir iklan dinonaktifkan"
        )
    }

    fun toggleCurrentSiteWhitelist() {
        val currentHost = AdBlockerEngine.extractHost(_uiState.value.currentUrl) ?: return
        viewModelScope.launch {
            val currentlyWhitelisted = _uiState.value.isCurrentPageWhitelisted
            if (currentlyWhitelisted) {
                repository.removeWhitelisted(currentHost)
                updateActiveTab { it.copy(isWhitelisted = false) }
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = "$currentHost dihapus dari daftar putih"
                )
            } else {
                repository.addWhitelisted(currentHost)
                updateActiveTab { it.copy(isWhitelisted = true) }
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = "$currentHost ditambahkan ke daftar putih"
                )
            }
        }
    }

    fun addDomainToWhitelist(domain: String) {
        val cleanDomain = domain.trim().lowercase().removePrefix("https://").removePrefix("http://").removePrefix("www.")
        if (cleanDomain.isNotEmpty()) {
            viewModelScope.launch {
                repository.addWhitelisted(cleanDomain)
                _uiState.value = _uiState.value.copy(snackbarMessage = "$cleanDomain masuk daftar putih")
            }
        }
    }

    fun removeDomainFromWhitelist(domain: String) {
        viewModelScope.launch {
            repository.removeWhitelisted(domain)
            _uiState.value = _uiState.value.copy(snackbarMessage = "$domain dihapus dari daftar putih")
        }
    }

    fun recordAdBlocked(blockedUrl: String) {
        val pageHost = AdBlockerEngine.extractHost(_uiState.value.currentUrl) ?: "unknown"
        updateActiveTab { it.copy(pageBlockedCount = it.pageBlockedCount + 1) }
        viewModelScope.launch {
            repository.recordBlockedAd(pageHost, blockedUrl)
        }
    }

    fun toggleBookmark() {
        val url = _uiState.value.currentUrl
        val title = _uiState.value.pageTitle
        viewModelScope.launch {
            val isBookmarked = _uiState.value.isCurrentBookmarked
            if (isBookmarked) {
                val found = bookmarkEntries.value.find { it.url == url }
                if (found != null) {
                    repository.removeBookmark(found.id)
                }
                updateActiveTab { it.copy(isBookmarked = false) }
                _uiState.value = _uiState.value.copy(snackbarMessage = "Penanda dihapus")
            } else {
                repository.addBookmark(title, url)
                updateActiveTab { it.copy(isBookmarked = true) }
                _uiState.value = _uiState.value.copy(snackbarMessage = "Halaman ditambahkan ke penanda")
            }
        }
    }

    fun removeBookmark(id: Long) {
        viewModelScope.launch {
            repository.removeBookmark(id)
            _uiState.value = _uiState.value.copy(snackbarMessage = "Penanda dihapus")
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _uiState.value = _uiState.value.copy(snackbarMessage = "Riwayat penjelajahan dibersihkan")
        }
    }

    fun clearAlerts() {
        viewModelScope.launch {
            repository.clearAlerts()
            _uiState.value = _uiState.value.copy(snackbarMessage = "Log aktivitas jaringan dibersihkan")
        }
    }

    // --- Find In Page Handlers ---
    fun openFindInPage(open: Boolean) {
        _uiState.value = _uiState.value.copy(
            isFindInPageOpen = open,
            findQuery = if (!open) "" else _uiState.value.findQuery,
            findMatchIndex = if (!open) 0 else _uiState.value.findMatchIndex,
            findMatchCount = if (!open) 0 else _uiState.value.findMatchCount
        )
    }

    fun setFindQuery(query: String) {
        _uiState.value = _uiState.value.copy(findQuery = query)
    }

    fun updateFindMatches(activeMatchIndex: Int, numberOfMatches: Int) {
        _uiState.value = _uiState.value.copy(
            findMatchIndex = activeMatchIndex,
            findMatchCount = numberOfMatches
        )
    }

    // --- Zoom Settings Handlers ---
    fun openZoomDialog(open: Boolean) {
        _uiState.value = _uiState.value.copy(isZoomDialogOpen = open)
    }

    fun setTextZoomPercent(percent: Int) {
        _uiState.value = _uiState.value.copy(
            textZoomPercent = percent,
            snackbarMessage = "Ukuran teks diubah ke $percent%"
        )
    }

    // --- Quick Privacy & Cache Cleaner Handlers ---
    fun openClearDataDialog(open: Boolean) {
        _uiState.value = _uiState.value.copy(isClearDataDialogOpen = open)
    }

    fun performQuickPrivacyClear(
        clearHistory: Boolean,
        clearCache: Boolean,
        clearAlerts: Boolean
    ) {
        viewModelScope.launch {
            if (clearHistory) {
                repository.clearHistory()
            }
            if (clearAlerts) {
                repository.clearAlerts()
            }
            _uiState.value = _uiState.value.copy(
                isClearDataDialogOpen = false,
                snackbarMessage = "Data penjelajahan dan cache berhasil dibersihkan"
            )
        }
    }

    // --- QR Code Dialog Handlers ---
    fun openQrDialog(open: Boolean) {
        _uiState.value = _uiState.value.copy(isQrDialogOpen = open)
    }

    fun dismissSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    private fun checkCurrentPageWhitelistStatus(url: String) {
        val host = AdBlockerEngine.extractHost(url)
        val whitelistSet = whitelistedSites.value.map { it.domain }.toSet()
        val isWhitelisted = AdBlockerEngine.isWhitelisted(host, whitelistSet)
        val isBookmarked = bookmarkEntries.value.any { it.url == url }
        updateActiveTab { it.copy(isWhitelisted = isWhitelisted, isBookmarked = isBookmarked) }
    }
}
