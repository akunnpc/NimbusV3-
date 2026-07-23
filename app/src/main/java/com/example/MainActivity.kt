package com.example

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.BrowserViewModel
import com.example.ui.NavigationTab
import com.example.ui.components.BottomNavBar
import com.example.ui.components.BrowserWebView
import com.example.ui.components.ClearDataDialog
import com.example.ui.components.FindInPageBar
import com.example.ui.components.QrCodeDialog
import com.example.ui.components.SearchEngineDialog
import com.example.ui.components.TabSwitcherSheet
import com.example.ui.components.TopAddressBar
import com.example.ui.components.ZoomDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.ApiDocsScreen
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.HistoryBookmarksScreen
import com.example.ui.screens.SecurityAlertsScreen
import com.example.ui.screens.WhitelistScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                NimbusV3App()
            }
        }
    }
}

@Composable
fun NimbusV3App(viewModel: BrowserViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val whitelistedSites by viewModel.whitelistedSites.collectAsStateWithLifecycle()
    val todayBlockedCount by viewModel.todayBlockedCount.collectAsStateWithLifecycle()
    val totalBlockedCount by viewModel.totalBlockedCount.collectAsStateWithLifecycle()
    val monthlyBlockedCount by viewModel.monthlyBlockedCount.collectAsStateWithLifecycle()
    val dailySummaries by viewModel.dailySummaries.collectAsStateWithLifecycle()
    val topBlockedDomains by viewModel.topBlockedDomains.collectAsStateWithLifecycle()
    val historyList by viewModel.historyEntries.collectAsStateWithLifecycle()
    val bookmarksList by viewModel.bookmarkEntries.collectAsStateWithLifecycle()
    val securityAlerts by viewModel.securityAlerts.collectAsStateWithLifecycle()

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissSnackbar()
        }
    }

    val whitelistSet = remember(whitelistedSites) {
        whitelistedSites.map { it.domain }.toSet()
    }

    // Modal Overlays
    if (state.isTabSwitcherOpen) {
        TabSwitcherSheet(
            tabs = state.tabs,
            activeTabId = state.activeTabId,
            onSelectTab = viewModel::selectActiveTab,
            onCloseTab = viewModel::closeTab,
            onAddNewTab = { isIncognito -> viewModel.addNewTab(isIncognito) },
            onCloseAllTabs = viewModel::closeAllTabs,
            onDismiss = { viewModel.openTabSwitcher(false) }
        )
    }

    if (state.isSearchEngineDialogOpen) {
        SearchEngineDialog(
            currentEngine = state.searchEngine,
            onSelectEngine = viewModel::setSearchEngine,
            onDismiss = { viewModel.openSearchEngineDialog(false) }
        )
    }

    if (state.isZoomDialogOpen) {
        ZoomDialog(
            currentZoom = state.textZoomPercent,
            onSelectZoom = viewModel::setTextZoomPercent,
            onDismiss = { viewModel.openZoomDialog(false) }
        )
    }

    if (state.isClearDataDialogOpen) {
        ClearDataDialog(
            onClearData = { clearHistory, clearCache, clearAlerts ->
                if (clearCache) {
                    webViewInstance?.clearCache(true)
                    webViewInstance?.clearHistory()
                    android.webkit.CookieManager.getInstance().removeAllCookies(null)
                    android.webkit.WebStorage.getInstance().deleteAllData()
                }
                viewModel.performQuickPrivacyClear(clearHistory, clearCache, clearAlerts)
            },
            onDismiss = { viewModel.openClearDataDialog(false) }
        )
    }

    if (state.isQrDialogOpen) {
        QrCodeDialog(
            url = state.currentUrl,
            onDismiss = { viewModel.openQrDialog(false) }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAddressBar(
                state = state,
                onUrlInputChanged = viewModel::onUrlInputChanged,
                onLoadUrl = viewModel::loadUrl,
                onToggleAdBlocker = viewModel::toggleAdBlockerGlobal,
                onToggleWhitelist = viewModel::toggleCurrentSiteWhitelist,
                onToggleBookmark = viewModel::toggleBookmark,
                onReload = { webViewInstance?.reload() },
                onOpenTabSwitcher = { viewModel.openTabSwitcher(true) },
                onToggleIncognito = viewModel::toggleIncognitoForActiveTab,
                onToggleDesktopMode = viewModel::toggleDesktopModeForActiveTab,
                onToggleReaderMode = viewModel::toggleReaderModeForActiveTab,
                onToggleDataSaver = viewModel::toggleDataSaverMode,
                onToggleTurboMode = viewModel::toggleTurboMode,
                onToggleAutoTabHibernation = viewModel::toggleAutoTabHibernation,
                onOpenFindInPage = { viewModel.openFindInPage(true) },
                onOpenZoomDialog = { viewModel.openZoomDialog(true) },
                onOpenClearDataDialog = { viewModel.openClearDataDialog(true) },
                onOpenQrDialog = { viewModel.openQrDialog(true) },
                onOpenSearchEngineDialog = { viewModel.openSearchEngineDialog(true) },
                onOpenDownloads = { viewModel.selectTab(NavigationTab.DOWNLOADS) }
            )
        },
        bottomBar = {
            BottomNavBar(
                selectedTab = state.selectedTab,
                alertsCount = securityAlerts.size,
                downloadsCount = state.downloads.size,
                onTabSelected = viewModel::selectTab
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.selectedTab) {
                NavigationTab.BROWSER -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        BrowserWebView(
                            state = state,
                            whitelistedSites = whitelistSet,
                            onPageStarted = viewModel::onPageStarted,
                            onPageFinished = viewModel::onPageFinished,
                            onPageProgress = viewModel::onPageProgress,
                            onAdBlocked = viewModel::recordAdBlocked,
                            onDownloadStarted = viewModel::addDownload,
                            onFindMatches = viewModel::updateFindMatches,
                            webViewRef = { webViewInstance = it }
                        )

                        if (state.isFindInPageOpen) {
                            FindInPageBar(
                                query = state.findQuery,
                                matchIndex = state.findMatchIndex,
                                matchCount = state.findMatchCount,
                                onQueryChange = viewModel::setFindQuery,
                                onFindNext = { forward ->
                                    webViewInstance?.findNext(forward)
                                },
                                onClose = { viewModel.openFindInPage(false) },
                                modifier = Modifier.align(androidx.compose.ui.Alignment.TopCenter)
                            )
                        }
                    }
                }

                NavigationTab.ANALYTICS -> {
                    AnalyticsScreen(
                        todayCount = todayBlockedCount,
                        totalCount = totalBlockedCount,
                        monthlyCount = monthlyBlockedCount,
                        dailySummaries = dailySummaries,
                        topDomains = topBlockedDomains,
                        onExportReport = {
                            viewModel.loadUrl("about:blank")
                            viewModel.selectTab(NavigationTab.ANALYTICS)
                        }
                    )
                }

                NavigationTab.WHITELIST -> {
                    WhitelistScreen(
                        sites = whitelistedSites,
                        onAddDomain = viewModel::addDomainToWhitelist,
                        onRemoveDomain = viewModel::removeDomainFromWhitelist
                    )
                }

                NavigationTab.HISTORY -> {
                    HistoryBookmarksScreen(
                        historyList = historyList,
                        bookmarksList = bookmarksList,
                        onOpenUrl = viewModel::loadUrl,
                        onClearHistory = viewModel::clearHistory,
                        onRemoveBookmark = viewModel::removeBookmark
                    )
                }

                NavigationTab.DOWNLOADS -> {
                    DownloadsScreen(
                        downloads = state.downloads,
                        onRemoveDownload = viewModel::removeDownload,
                        onClearAll = viewModel::clearDownloads
                    )
                }

                NavigationTab.ALERTS -> {
                    SecurityAlertsScreen(
                        alerts = securityAlerts,
                        onClearAlerts = viewModel::clearAlerts
                    )
                }

                NavigationTab.API_DOCS -> {
                    ApiDocsScreen()
                }
            }
        }
    }
}
