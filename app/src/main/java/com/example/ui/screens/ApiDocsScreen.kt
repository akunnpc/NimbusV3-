package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.IntegrationInstructions
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ApiDocsScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Dokumentasi Arsitektur & API",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Panduan teknis pengembang untuk NimbusV3 AdBlock Engine",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Section 1: WebRequest Interception API
        item {
            DocCard(
                title = "1. webRequest API & Content Interception",
                icon = Icons.Default.IntegrationInstructions
            ) {
                Text(
                    text = "Pemblokiran iklan otomatis bekerja melalui pencegatan resource tingkat jaringan pada WebViewClient.shouldInterceptRequest().",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                CodeSnippet(
                    code = """
override fun shouldInterceptRequest(
    view: WebView?,
    request: WebResourceRequest?
): WebResourceResponse? {
    val reqUrl = request.url.toString()
    if (AdBlockerEngine.isAdRequest(reqUrl, pageHost, whitelist, isAdBlockEnabled)) {
        viewModel.recordAdBlocked(reqUrl)
        return AdBlockerEngine.createEmptyResponse()
    }
    return super.shouldInterceptRequest(view, request)
}
                    """.trimIndent()
                )
            }
        }

        // Section 2: AdBlockerEngine Rules
        item {
            DocCard(
                title = "2. AdBlockerEngine Matching Algorithm",
                icon = Icons.Default.Code
            ) {
                Text(
                    text = "AdBlockerEngine mengevaluasi URL berdasarkan 3 layer pencegatan:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Layer 1: Pengecekan Daftar Putih (Whitelist) situs utama. Jika domain aktif ada di daftar putih, resource diizinkan.\n• Layer 2: Penyesuaian domain server iklan terkenal (e.g. doubleclick.net, googlesyndication.com, adnxs.com).\n• Layer 3: Pengecekan pola skrip pihak ketiga (/ads/, /pop.js, tracking.js).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Section 3: Session & Persistence
        item {
            DocCard(
                title = "3. Manajemen Sesi & Persistence",
                icon = Icons.Default.Storage
            ) {
                Text(
                    text = "Sesi autentikasi dan cookie pengguna disimpan persisten menggunakan CookieManager Android dan Room SQLite Database.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                CodeSnippet(
                    code = """
CookieManager.getInstance().run {
    setAcceptCookie(true)
    setAcceptThirdPartyCookies(webView, true)
    flush()
}
                    """.trimIndent()
                )
            }
        }
    }
}

@Composable
private fun DocCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("doc_card_${title.take(10)}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun CodeSnippet(code: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = code,
            modifier = Modifier.padding(12.dp),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
