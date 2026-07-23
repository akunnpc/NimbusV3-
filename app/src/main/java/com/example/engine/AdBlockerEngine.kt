package com.example.engine

import android.net.Uri
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream

object AdBlockerEngine {

    // Known ad servers, tracking networks, popup scripts, and telemetry hosts
    private val AD_HOSTS: Set<String> = setOf(
        "doubleclick.net",
        "googleadservices.com",
        "googlesyndication.com",
        "adservice.google.com",
        "pagead2.googlesyndication.com",
        "adnxs.com",
        "taboola.com",
        "outbrain.com",
        "popads.net",
        "popcash.net",
        "criteo.com",
        "scorecardresearch.com",
        "moatads.com",
        "amazon-adsystem.com",
        "pubmatic.com",
        "rubiconproject.com",
        "casalemedia.com",
        "bidswitch.net",
        "openx.net",
        "smartadserver.com",
        "exponential.com",
        "adroll.formstack.com",
        "adform.net",
        "buysellads.com",
        "propellerads.com",
        "exoclick.com",
        "adcolony.com",
        "unityads.unity3d.com",
        "applovin.com",
        "chartbeat.com",
        "hotjar.com",
        "clarity.ms",
        "segment.io",
        "mixpanel.com"
    )

    // Common URL path patterns for ad scripts and banners
    private val AD_URL_PATTERNS = listOf(
        "/ads/",
        "/ad/",
        "/banner/",
        "/banners/",
        "/pop.js",
        "/popunder.",
        "/adserver/",
        "/pagead/",
        "google_ads",
        "affiliate_",
        "tracking.js",
        "adframe.",
        "advert."
    )

    /**
     * Extracts normalized domain from a URL string (e.g. "https://sub.example.com/path" -> "example.com")
     */
    fun extractHost(url: String?): String? {
        if (url.isNullOrEmpty()) return null
        return try {
            val cleanUrl = if (!url.contains("://")) "http://$url" else url
            val javaUri = java.net.URI(cleanUrl)
            var host = javaUri.host?.lowercase()
            if (host == null) {
                // Fallback manual string parsing
                var temp = url.trim().lowercase()
                if (temp.contains("://")) {
                    temp = temp.substringAfter("://")
                }
                temp = temp.substringBefore("/").substringBefore("?").substringBefore("#").substringBefore(":")
                host = temp
            }
            if (host.startsWith("www.")) {
                host = host.substring(4)
            }
            if (host.isNotEmpty()) host else null
        } catch (e: Exception) {
            try {
                var temp = url.trim().lowercase()
                if (temp.contains("://")) {
                    temp = temp.substringAfter("://")
                }
                temp = temp.substringBefore("/").substringBefore("?").substringBefore("#").substringBefore(":")
                if (temp.startsWith("www.")) {
                    temp = temp.substring(4)
                }
                if (temp.isNotEmpty()) temp else null
            } catch (e2: Exception) {
                null
            }
        }
    }

    /**
     * Checks if current page domain is in whitelist
     */
    fun isWhitelisted(siteHost: String?, whitelist: Set<String>): Boolean {
        if (siteHost.isNullOrEmpty()) return false
        val host = siteHost.lowercase().removePrefix("www.")
        return whitelist.any { whitelisted ->
            val clean = whitelisted.lowercase().removePrefix("www.")
            host == clean || host.endsWith(".$clean")
        }
    }

    /**
     * Main check: evaluates if a resource request URL is an ad/tracker
     */
    fun isAdRequest(
        requestUrl: String?,
        currentSiteHost: String?,
        whitelist: Set<String>,
        adBlockEnabled: Boolean = true
    ): Boolean {
        if (!adBlockEnabled) return false
        if (requestUrl.isNullOrEmpty()) return false

        // Check if user whitelisted the current top-level site
        if (isWhitelisted(currentSiteHost, whitelist)) {
            return false
        }

        val requestHost = extractHost(requestUrl) ?: return false

        // 1. Direct host match or sub-domain match
        val isBlockedHost = AD_HOSTS.any { adHost ->
            requestHost == adHost || requestHost.endsWith(".$adHost")
        }
        if (isBlockedHost) return true

        // 2. Pattern matching in URL path for distinct third-party ad scripts
        if (currentSiteHost != null && requestHost != currentSiteHost) {
            val lowerUrl = requestUrl.lowercase()
            val matchesPattern = AD_URL_PATTERNS.any { pattern -> lowerUrl.contains(pattern) }
            if (matchesPattern) return true
        }

        return false
    }

    /**
     * Creates an empty HTTP response to prevent the ad network script or banner from loading
     */
    fun createEmptyResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "UTF-8",
            200,
            "OK",
            mapOf("Access-Control-Allow-Origin" to "*"),
            ByteArrayInputStream(ByteArray(0))
        )
    }
}
