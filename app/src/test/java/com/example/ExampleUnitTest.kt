package com.example

import com.example.engine.AdBlockerEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `test host extraction`() {
        assertEquals("example.com", AdBlockerEngine.extractHost("https://www.example.com/path?q=1"))
        assertEquals("sub.domain.co.id", AdBlockerEngine.extractHost("http://sub.domain.co.id/news"))
        assertEquals(null, AdBlockerEngine.extractHost(""))
    }

    @Test
    fun `test ad request detection`() {
        val whitelist = setOf<String>()

        // Known ad domain
        assertTrue(
            AdBlockerEngine.isAdRequest(
                requestUrl = "https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js",
                currentSiteHost = "news.org",
                whitelist = whitelist
            )
        )

        // Third party tracking script
        assertTrue(
            AdBlockerEngine.isAdRequest(
                requestUrl = "https://analytics-tracker.net/pop.js",
                currentSiteHost = "mywebsite.com",
                whitelist = whitelist
            )
        )

        // Standard site resource
        assertFalse(
            AdBlockerEngine.isAdRequest(
                requestUrl = "https://mywebsite.com/images/logo.png",
                currentSiteHost = "mywebsite.com",
                whitelist = whitelist
            )
        )
    }

    @Test
    fun `test whitelist exemption`() {
        val whitelist = setOf("favorite-creator.com")

        // Request from whitelisted site should NOT be blocked
        assertFalse(
            AdBlockerEngine.isAdRequest(
                requestUrl = "https://doubleclick.net/ad.js",
                currentSiteHost = "favorite-creator.com",
                whitelist = whitelist
            )
        )
    }
}
