package com.example.ui.components

import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import com.example.engine.AdBlockerEngine
import com.example.engine.SessionManager
import com.example.ui.BrowserUiState

@Composable
fun BrowserWebView(
    state: BrowserUiState,
    whitelistedSites: Set<String>,
    onPageStarted: (String) -> Unit,
    onPageFinished: (String, String?) -> Unit,
    onPageProgress: (Int) -> Unit,
    onAdBlocked: (String) -> Unit,
    onDownloadStarted: (fileName: String, url: String, mimeType: String, fileSize: String) -> Unit,
    onFindMatches: (activeMatchIndex: Int, numberOfMatches: Int) -> Unit = { _, _ -> },
    webViewRef: (WebView?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            SessionManager.configureWebViewSession(this)
        }
    }

    DisposableEffect(Unit) {
        webViewRef(webView)
        onDispose {
            webViewRef(null)
            webView.destroy()
        }
    }

    // Configure Download Listener
    DisposableEffect(webView) {
        webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            try {
                val fileName = URLUtil.guessFileName(url, contentDisposition, mimetype)
                val request = DownloadManager.Request(Uri.parse(url)).apply {
                    setMimeType(mimetype)
                    val cookie = CookieManager.getInstance().getCookie(url)
                    addRequestHeader("cookie", cookie)
                    addRequestHeader("User-Agent", userAgent)
                    setDescription("Mengunduh berkas via NimbusV3 Browser")
                    setTitle(fileName)
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                }
                val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                dm?.enqueue(request)

                val formattedSize = if (contentLength > 0) "${contentLength / 1024} KB" else "Ukuran tidak diketahui"
                onDownloadStarted(fileName, url, mimetype ?: "application/octet-stream", formattedSize)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        onDispose { webView.setDownloadListener(null) }
    }

    // Configure Incognito Mode vs Normal Mode
    LaunchedEffect(state.isIncognito) {
        if (state.isIncognito) {
            webView.settings.savePassword = false
            webView.settings.saveFormData = false
            webView.clearCache(true)
            webView.clearHistory()
            CookieManager.getInstance().setAcceptCookie(false)
        } else {
            webView.settings.savePassword = true
            webView.settings.saveFormData = true
            CookieManager.getInstance().setAcceptCookie(true)
        }
    }

    // Configure Data Saver Mode
    LaunchedEffect(state.isDataSaverEnabled) {
        webView.settings.blockNetworkImage = state.isDataSaverEnabled
        webView.settings.loadsImagesAutomatically = !state.isDataSaverEnabled
        if (state.currentUrl.isNotEmpty()) {
            webView.reload()
        }
    }

    // Configure Turbo Booster Mode
    LaunchedEffect(state.isTurboModeEnabled) {
        if (state.isTurboModeEnabled) {
            webView.settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
            webView.setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
        } else {
            webView.settings.cacheMode = android.webkit.WebSettings.LOAD_NORMAL
        }
    }

    // Configure Auto Tab Hibernation on Lifecycle Pause/Resume
    DisposableEffect(state.isAutoTabHibernationEnabled) {
        onDispose {
            if (state.isAutoTabHibernationEnabled) {
                webView.onPause()
                webView.pauseTimers()
            }
        }
    }

    // Configure Text Zoom & Font Scaling
    LaunchedEffect(state.textZoomPercent) {
        webView.settings.textZoom = state.textZoomPercent
    }

    // Configure Find in Page Search
    DisposableEffect(Unit) {
        webView.setFindListener { activeMatch, numberOfMatches, _ ->
            onFindMatches(activeMatch, numberOfMatches)
        }
        onDispose { webView.setFindListener(null) }
    }

    LaunchedEffect(state.findQuery) {
        if (state.findQuery.isNotEmpty()) {
            webView.findAllAsync(state.findQuery)
        } else {
            webView.clearMatches()
        }
    }

    // Configure Desktop Mode vs Mobile Mode
    LaunchedEffect(state.isDesktopMode) {
        if (state.isDesktopMode) {
            webView.settings.userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            webView.settings.useWideViewPort = true
            webView.settings.loadWithOverviewMode = true
        } else {
            webView.settings.userAgentString = null
            webView.settings.useWideViewPort = false
            webView.settings.loadWithOverviewMode = false
        }
        if (state.currentUrl.isNotEmpty()) {
            webView.reload()
        }
    }

    // Configure Reader Mode injection
    LaunchedEffect(state.isReaderMode, state.currentUrl) {
        if (state.isReaderMode) {
            val readerJs = """
                javascript:(function() {
                    var style = document.createElement('style');
                    style.innerHTML = 'body { background-color: #0F172A !important; color: #F8FAFC !important; font-family: system-ui, sans-serif !important; max-width: 800px !important; margin: 0 auto !important; padding: 20px !important; line-height: 1.7 !important; } a { color: #38BDF8 !important; } img { max-width: 100% !important; height: auto !important; border-radius: 12px !important; margin: 12px 0 !important; } header, footer, nav, aside, .ad, .ads, .banner, iframe { display: none !important; }';
                    document.head.appendChild(style);
                })();
            """.trimIndent()
            webView.evaluateJavascript(readerJs, null)
        }
    }

    // Set WebViewClients
    LaunchedEffect(state.isAdBlockEnabled, whitelistedSites) {
        webView.webViewClient = object : WebViewClient() {

            private var currentHost: String? = null

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                url?.let {
                    currentHost = AdBlockerEngine.extractHost(it)
                    onPageStarted(it)
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                url?.let {
                    onPageFinished(it, view?.title)
                }
                SessionManager.flushSession()

                // Re-apply reader mode if active
                if (state.isReaderMode) {
                    val readerJs = """
                        javascript:(function() {
                            var style = document.createElement('style');
                            style.innerHTML = 'body { background-color: #0F172A !important; color: #F8FAFC !important; font-family: system-ui, sans-serif !important; max-width: 800px !important; margin: 0 auto !important; padding: 20px !important; line-height: 1.7 !important; } a { color: #38BDF8 !important; } img { max-width: 100% !important; height: auto !important; border-radius: 12px !important; margin: 12px 0 !important; } header, footer, nav, aside, .ad, .ads, .banner, iframe { display: none !important; }';
                            document.head.appendChild(style);
                        })();
                    """.trimIndent()
                    view?.evaluateJavascript(readerJs, null)
                }
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                if (request == null) return null

                val reqUrl = request.url.toString()
                val host = AdBlockerEngine.extractHost(state.currentUrl) ?: currentHost

                // Check ad blocker engine
                if (AdBlockerEngine.isAdRequest(
                        requestUrl = reqUrl,
                        currentSiteHost = host,
                        whitelist = whitelistedSites,
                        adBlockEnabled = state.isAdBlockEnabled
                    )
                ) {
                    webView.post {
                        onAdBlocked(reqUrl)
                    }
                    return AdBlockerEngine.createEmptyResponse()
                }

                return null
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                onPageProgress(newProgress)
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                view?.url?.let { onPageFinished(it, title) }
            }
        }
    }

    // Load URL whenever state.currentUrl changes and differs from webView.url
    LaunchedEffect(state.currentUrl) {
        if (state.currentUrl.isNotEmpty() && webView.url != state.currentUrl) {
            webView.loadUrl(state.currentUrl)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { webView },
            modifier = Modifier
                .fillMaxSize()
                .testTag("browser_web_view")
        )

        if (state.currentUrl.isEmpty() || state.currentUrl == "about:blank") {
            BrowserHomeScreen(
                state = state,
                onNavigateUrl = { targetUrl ->
                    webView.loadUrl(targetUrl)
                }
            )
        }
    }
}
