package com.example.engine

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView

object SessionManager {

    /**
     * Initializes WebView settings for persistent user sessions, logins, and storage.
     */
    fun configureWebViewSession(webView: WebView) {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false

        // Enable third-party cookies for seamless login sessions across OAuth providers
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)
    }

    /**
     * Flushes cookies to disk storage ensuring sessions persist across app restarts
     */
    fun flushSession() {
        try {
            CookieManager.getInstance().flush()
        } catch (e: Exception) {
            // Ignore
        }
    }

    /**
     * Clears all session data (cookies, local storage, web cache) on demand
     */
    fun clearAllSessionData(context: Context, webView: WebView?, onCleared: () -> Unit) {
        try {
            webView?.clearCache(true)
            webView?.clearHistory()
            CookieManager.getInstance().removeAllCookies {
                WebStorage.getInstance().deleteAllData()
                onCleared()
            }
        } catch (e: Exception) {
            onCleared()
        }
    }
}
