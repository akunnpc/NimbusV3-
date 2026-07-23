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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NavigationTab {
    BROWSER,
    ANALYTICS,
    WHITELIST,
    HISTORY,
    ALERTS,
    API_DOCS
}

data class BrowserUiState(
    val currentUrl: String = "https://www.wikipedia.org",
    val urlInput: String = "https://www.wikipedia.org",
    val pageTitle: String = "Wikipedia, the free encyclopedia",
    val pageProgress: Int = 100,
    val isLoading: Boolean = false,
    val isAdBlockEnabled: Boolean = true,
    val pageBlockedCount: Int = 0,
    val isCurrentPageWhitelisted: Boolean = false,
    val selectedTab: NavigationTab = NavigationTab.BROWSER,
    val snackbarMessage: String? = null,
    val isCurrentBookmarked: Boolean = false
)

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
        // Observe whitelist changes to update current site whitelist status
        viewModelScope.launch {
            whitelistedSites.collect { sites ->
                val currentHost = AdBlockerEngine.extractHost(_uiState.value.currentUrl)
                val whitelistSet = sites.map { it.domain }.toSet()
                val isWhitelisted = AdBlockerEngine.isWhitelisted(currentHost, whitelistSet)
                _uiState.value = _uiState.value.copy(isCurrentPageWhitelisted = isWhitelisted)
            }
        }
    }

    fun selectTab(tab: NavigationTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun onUrlInputChanged(input: String) {
        _uiState.value = _uiState.value.copy(urlInput = input)
    }

    fun loadUrl(inputUrl: String) {
        var formatted = inputUrl.trim()
        if (formatted.isEmpty()) return

        if (!formatted.startsWith("http://") && !formatted.startsWith("https://")) {
            formatted = if (formatted.contains(".") && !formatted.contains(" ")) {
                "https://$formatted"
            } else {
                "https://www.google.com/search?q=${java.net.URLEncoder.encode(formatted, "UTF-8")}"
            }
        }

        _uiState.value = _uiState.value.copy(
            currentUrl = formatted,
            urlInput = formatted,
            pageBlockedCount = 0,
            isLoading = true,
            selectedTab = NavigationTab.BROWSER
        )
        checkCurrentPageWhitelistStatus(formatted)
    }

    fun onPageStarted(url: String) {
        val host = AdBlockerEngine.extractHost(url)
        val whitelistSet = whitelistedSites.value.map { it.domain }.toSet()
        val isWhitelisted = AdBlockerEngine.isWhitelisted(host, whitelistSet)

        _uiState.value = _uiState.value.copy(
            currentUrl = url,
            urlInput = url,
            pageBlockedCount = 0,
            isLoading = true,
            isCurrentPageWhitelisted = isWhitelisted
        )
    }

    fun onPageFinished(url: String, title: String?) {
        val finalTitle = if (!title.isNullOrEmpty() && title != "about:blank") title else url
        _uiState.value = _uiState.value.copy(
            currentUrl = url,
            urlInput = url,
            pageTitle = finalTitle,
            isLoading = false,
            pageProgress = 100
        )
        // Record history
        viewModelScope.launch {
            repository.addHistory(finalTitle, url)
        }
    }

    fun onPageProgress(progress: Int) {
        _uiState.value = _uiState.value.copy(
            pageProgress = progress,
            isLoading = progress < 100
        )
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
                _uiState.value = _uiState.value.copy(
                    isCurrentPageWhitelisted = false,
                    snackbarMessage = "$currentHost dihapus dari daftar putih"
                )
            } else {
                repository.addWhitelisted(currentHost)
                _uiState.value = _uiState.value.copy(
                    isCurrentPageWhitelisted = true,
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
        _uiState.value = _uiState.value.copy(
            pageBlockedCount = _uiState.value.pageBlockedCount + 1
        )
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
                // Find and delete
                val found = bookmarkEntries.value.find { it.url == url }
                if (found != null) {
                    repository.removeBookmark(found.id)
                }
                _uiState.value = _uiState.value.copy(
                    isCurrentBookmarked = false,
                    snackbarMessage = "Penanda dihapus"
                )
            } else {
                repository.addBookmark(title, url)
                _uiState.value = _uiState.value.copy(
                    isCurrentBookmarked = true,
                    snackbarMessage = "Halaman ditambahkan ke penanda"
                )
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

    fun dismissSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    private fun checkCurrentPageWhitelistStatus(url: String) {
        val host = AdBlockerEngine.extractHost(url)
        val whitelistSet = whitelistedSites.value.map { it.domain }.toSet()
        val isWhitelisted = AdBlockerEngine.isWhitelisted(host, whitelistSet)
        val isBookmarked = bookmarkEntries.value.any { it.url == url }
        _uiState.value = _uiState.value.copy(
            isCurrentPageWhitelisted = isWhitelisted,
            isCurrentBookmarked = isBookmarked
        )
    }
}
