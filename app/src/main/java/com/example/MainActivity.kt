package com.example

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import com.example.ui.BrowserUiState
import com.example.ui.BrowserViewModel
import com.example.ui.NavigationTab
import com.example.ui.components.BottomNavBar
import com.example.ui.components.BrowserWebView
import com.example.ui.components.TopAddressBar
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.ApiDocsScreen
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
                ShieldBrowserApp()
            }
        }
    }
}

@Composable
fun ShieldBrowserApp(viewModel: BrowserViewModel = viewModel()) {
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
                onReload = { webViewInstance?.reload() }
            )
        },
        bottomBar = {
            BottomNavBar(
                selectedTab = state.selectedTab,
                alertsCount = securityAlerts.size,
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
                    BrowserWebView(
                        state = state,
                        whitelistedSites = whitelistSet,
                        onPageStarted = viewModel::onPageStarted,
                        onPageFinished = viewModel::onPageFinished,
                        onPageProgress = viewModel::onPageProgress,
                        onAdBlocked = viewModel::recordAdBlocked,
                        webViewRef = { webViewInstance = it }
                    )
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
