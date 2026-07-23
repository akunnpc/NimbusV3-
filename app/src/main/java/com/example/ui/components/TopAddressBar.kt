package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BrowserUiState

@Composable
fun TopAddressBar(
    state: BrowserUiState,
    onUrlInputChanged: (String) -> Unit,
    onLoadUrl: (String) -> Unit,
    onToggleAdBlocker: () -> Unit,
    onToggleWhitelist: () -> Unit,
    onToggleBookmark: () -> Unit,
    onReload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val isHttps = state.currentUrl.startsWith("https://")

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ad Shield Icon with badge
                IconButton(
                    onClick = onToggleAdBlocker,
                    modifier = Modifier.testTag("toggle_ad_blocker_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (state.isAdBlockEnabled && state.pageBlockedCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ) {
                                    Text(
                                        text = if (state.pageBlockedCount > 99) "99+" else state.pageBlockedCount.toString(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Ad Shield",
                            tint = if (state.isAdBlockEnabled) {
                                if (state.isCurrentPageWhitelisted) Color(0xFFE65100) else Color(0xFF2E7D32)
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Address input bar
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .border(
                            width = 1.dp,
                            color = if (state.isCurrentPageWhitelisted) Color(0xFFFFA726) else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .clip(RoundedCornerShape(24.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // SSL Lock indicator
                        Icon(
                            imageVector = if (isHttps) Icons.Default.Lock else Icons.Default.Warning,
                            contentDescription = if (isHttps) "Secure SSL Connection" else "Unencrypted Connection",
                            tint = if (isHttps) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // URL Input text field
                        OutlinedTextField(
                            value = state.urlInput,
                            onValueChange = onUrlInputChanged,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("url_input_field"),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            placeholder = {
                                Text(
                                    "Ketik URL atau cari web...",
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                disabledBorderColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(onGo = {
                                focusManager.clearFocus()
                                onLoadUrl(state.urlInput)
                            })
                        )

                        if (state.urlInput.isNotEmpty()) {
                            IconButton(
                                onClick = { onUrlInputChanged("") },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hapus URL",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Whitelist toggle button
                IconButton(
                    onClick = onToggleWhitelist,
                    modifier = Modifier.testTag("whitelist_toggle_button")
                ) {
                    Icon(
                        imageVector = if (state.isCurrentPageWhitelisted) Icons.Default.Warning else Icons.Default.Security,
                        contentDescription = if (state.isCurrentPageWhitelisted) "Situs dalam Daftar Putih" else "Situs Dilindungi Pemblokir",
                        tint = if (state.isCurrentPageWhitelisted) Color(0xFFF57C00) else Color(0xFF1976D2)
                    )
                }

                // Bookmark toggle
                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.testTag("bookmark_button")
                ) {
                    Icon(
                        imageVector = if (state.isCurrentBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Simpan Penanda",
                        tint = if (state.isCurrentBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Reload button
                IconButton(
                    onClick = onReload,
                    modifier = Modifier.testTag("reload_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Muat Ulang Halaman"
                    )
                }
            }

            // Linear Progress Bar during page loading
            if (state.isLoading) {
                LinearProgressIndicator(
                    progress = { state.pageProgress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}
