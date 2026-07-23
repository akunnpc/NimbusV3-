package com.example.ui.components

import android.graphics.Bitmap
import android.os.Build
import android.view.ViewGroup
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
    }
}
