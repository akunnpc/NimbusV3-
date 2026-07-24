package com.example

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // System Back Press Navigation Handler
    val hasOpenDialog = state.isTabSwitcherOpen ||
            state.isSearchEngineDialogOpen ||
            state.isZoomDialogOpen ||
            state.isClearDataDialogOpen ||
            state.isQrDialogOpen ||
            state.isFindInPageOpen

    val isNotOnBrowserTab = state.selectedTab != NavigationTab.BROWSER
    val isOnWebPage = state.currentUrl.isNotEmpty() && state.currentUrl != "about:blank"
    val canGoBackInWeb = webViewInstance?.canGoBack() == true

    val shouldHandleBack = hasOpenDialog || isNotOnBrowserTab || canGoBackInWeb || isOnWebPage

    BackHandler(enabled = shouldHandleBack) {
        when {
            state.isTabSwitcherOpen -> viewModel.openTabSwitcher(false)
            state.isSearchEngineDialogOpen -> viewModel.openSearchEngineDialog(false)
            state.isZoomDialogOpen -> viewModel.openZoomDialog(false)
            state.isClearDataDialogOpen -> viewModel.openClearDataDialog(false)
            state.isQrDialogOpen -> viewModel.openQrDialog(false)
            state.isFindInPageOpen -> viewModel.openFindInPage(false)
            state.selectedTab != NavigationTab.BROWSER -> viewModel.selectTab(NavigationTab.BROWSER)
            webViewInstance?.canGoBack() == true -> webViewInstance?.goBack()
            state.currentUrl.isNotEmpty() && state.currentUrl != "about:blank" -> viewModel.loadUrl("")
        }
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

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < -20f) {
                    viewModel.setBottomBarVisible(false)
                } else if (delta > 20f) {
                    viewModel.setBottomBarVisible(true)
                }
                return Offset.Zero
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
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
            AnimatedVisibility(
                visible = state.isBottomBarVisible,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                BottomNavBar(
                    selectedTab = state.selectedTab,
                    alertsCount = securityAlerts.size,
                    downloadsCount = state.downloads.size,
                    onTabSelected = viewModel::selectTab
                )
            }
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
                            onScrollDirectionChanged = viewModel::setBottomBarVisible,
                            onToggleIncognito = viewModel::toggleIncognitoForActiveTab,
                            onOpenQrDialog = { viewModel.openQrDialog(true) },
                            onOpenSearchEngineDialog = { viewModel.openSearchEngineDialog(true) },
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
                                modifier = Modifier.align(Alignment.TopCenter)
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

            // Floating pill button to quickly reveal bottom bar when auto-hidden
            AnimatedVisibility(
                visible = !state.isBottomBarVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                Surface(
                    onClick = { viewModel.setBottomBarVisible(true) },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Tampilkan Menu",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Menu",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}
