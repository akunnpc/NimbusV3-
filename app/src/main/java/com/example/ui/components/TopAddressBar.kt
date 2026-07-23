package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.QrCode2

@Composable
fun TopAddressBar(
    state: BrowserUiState,
    onUrlInputChanged: (String) -> Unit,
    onLoadUrl: (String) -> Unit,
    onToggleAdBlocker: () -> Unit,
    onToggleWhitelist: () -> Unit,
    onToggleBookmark: () -> Unit,
    onReload: () -> Unit,
    onOpenTabSwitcher: () -> Unit,
    onToggleIncognito: () -> Unit,
    onToggleDesktopMode: () -> Unit,
    onToggleReaderMode: () -> Unit,
    onToggleDataSaver: () -> Unit,
    onToggleTurboMode: () -> Unit = {},
    onToggleAutoTabHibernation: () -> Unit = {},
    onOpenFindInPage: () -> Unit,
    onOpenZoomDialog: () -> Unit,
    onOpenClearDataDialog: () -> Unit,
    onOpenQrDialog: () -> Unit,
    onOpenSearchEngineDialog: () -> Unit,
    onOpenDownloads: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val isHttps = state.currentUrl.startsWith("https://")
    var isMenuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = if (state.isIncognito) Color(0xFF0F172A) else MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Incognito Status Bar Banner
            AnimatedVisibility(visible = state.isIncognito) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF0284C7)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mode Penyamaran Aktif • Riwayat & Data Sesi Tidak Disimpan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Data Saver Banner
            AnimatedVisibility(visible = state.isDataSaverEnabled && !state.isIncognito) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Penghemat Kuota (Data Saver) Aktif • Memuat hingga 70% lebih cepat",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
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
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Address input bar
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .border(
                            width = 1.dp,
                            color = if (state.isCurrentPageWhitelisted) Color(0xFFFFA726) else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(22.dp)
                        )
                        .clip(RoundedCornerShape(22.dp)),
                    color = if (state.isIncognito) Color(0xFF1E293B) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                            contentDescription = if (isHttps) "SSL Aman" else "Koneksi Tidak Terenkripsi",
                            tint = if (isHttps) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                            modifier = Modifier.size(15.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        // Search Engine indicator button
                        Surface(
                            modifier = Modifier.clickable { onOpenSearchEngineDialog() },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = state.searchEngine.displayName.take(1),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

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
                                color = if (state.isIncognito) Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            placeholder = {
                                Text(
                                    "Ketik URL / cari via ${state.searchEngine.displayName}...",
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (state.isIncognito) Color.LightGray else MaterialTheme.colorScheme.onSurfaceVariant
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

                        if (state.activeTab.lastRenderDurationMs > 0 && !state.isLoading) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Text(
                                    text = "⚡ ${state.activeTab.lastRenderDurationMs}ms",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (state.urlInput.isNotEmpty()) {
                            IconButton(
                                onClick = { onUrlInputChanged("") },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hapus URL",
                                    tint = if (state.isIncognito) Color.LightGray else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }

                // Tab Switcher Button
                IconButton(
                    onClick = onOpenTabSwitcher,
                    modifier = Modifier.testTag("tab_switcher_button")
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.5.dp,
                            color = if (state.isIncognito) Color(0xFF38BDF8) else MaterialTheme.colorScheme.primary
                        ),
                        color = Color.Transparent,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${state.tabs.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state.isIncognito) Color(0xFF38BDF8) else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Overflow Menu Button (3-dots)
                Box {
                    IconButton(
                        onClick = { isMenuExpanded = true },
                        modifier = Modifier.testTag("overflow_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opsi Lainnya",
                            tint = if (state.isIncognito) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (state.isIncognito) "Matikan Incognito" else "Mode Penyamaran (Incognito)"
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = if (state.isIncognito) Color(0xFF0284C7) else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                isMenuExpanded = false
                                onToggleIncognito()
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (state.isDesktopMode) "Minta Situs Seluler" else "Minta Situs Desktop"
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DesktopWindows,
                                    contentDescription = null,
                                    tint = if (state.isDesktopMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                isMenuExpanded = false
                                onToggleDesktopMode()
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (state.isReaderMode) "Matikan Mode Baca" else "Mode Baca (Reader Mode)"
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = if (state.isReaderMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                isMenuExpanded = false
                                onToggleReaderMode()
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (state.isDataSaverEnabled) "Matikan Penghemat Kuota" else "Mode Penghemat Kuota (Fast Data Saver)"
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = if (state.isDataSaverEnabled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                isMenuExpanded = false
                                onToggleDataSaver()
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (state.isTurboModeEnabled) "Akselerasi Turbo GPU (Aktif)" else "Aktifkan Akselerasi Turbo GPU"
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = if (state.isTurboModeEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                isMenuExpanded = false
                                onToggleTurboMode()
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (state.isAutoTabHibernationEnabled) "Hibernasi Tab Pasif (Hemat RAM)" else "Aktifkan Hibernasi Tab Pasif"
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = if (state.isAutoTabHibernationEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                isMenuExpanded = false
                                onToggleAutoTabHibernation()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Cari Teks di Halaman (Find in Page)") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.FindInPage, contentDescription = null)
                            },
                            onClick = {
                                isMenuExpanded = false
                                onOpenFindInPage()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Ukuran Teks & Zoom (${state.textZoomPercent}%)") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.FormatSize, contentDescription = null)
                            },
                            onClick = {
                                isMenuExpanded = false
                                onOpenZoomDialog()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Kode QR & Bagikan Tautan") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.QrCode2, contentDescription = null)
                            },
                            onClick = {
                                isMenuExpanded = false
                                onOpenQrDialog()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Pembersih Privasi & Cache") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.CleaningServices, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            },
                            onClick = {
                                isMenuExpanded = false
                                onOpenClearDataDialog()
                            }
                        )

                        HorizontalDivider()

                        DropdownMenuItem(
                            text = { Text("Mesin Pencari (${state.searchEngine.displayName})") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null)
                            },
                            onClick = {
                                isMenuExpanded = false
                                onOpenSearchEngineDialog()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Pengelola Unduhan") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Download, contentDescription = null)
                            },
                            onClick = {
                                isMenuExpanded = false
                                onOpenDownloads()
                            }
                        )

                        HorizontalDivider()

                        DropdownMenuItem(
                            text = { Text(if (state.isCurrentBookmarked) "Hapus Penanda" else "Tambah Penanda") },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (state.isCurrentBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = null,
                                    tint = if (state.isCurrentBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                isMenuExpanded = false
                                onToggleBookmark()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Muat Ulang Halaman") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                            },
                            onClick = {
                                isMenuExpanded = false
                                onReload()
                            }
                        )
                    }
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
