package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BrowserUiState

data class QuickDialSite(
    val title: String,
    val url: String,
    val icon: ImageVector,
    val badgeColor: Color
)

val defaultQuickDialSites = listOf(
    QuickDialSite("Google", "https://www.google.com", Icons.Default.Search, Color(0xFF4285F4)),
    QuickDialSite("YouTube", "https://www.youtube.com", Icons.Default.PlayArrow, Color(0xFFFF0000)),
    QuickDialSite("Gemini AI", "https://gemini.google.com", Icons.Default.AutoAwesome, Color(0xFF8E24AA)),
    QuickDialSite("GitHub", "https://github.com", Icons.Default.Code, Color(0xFF24292E)),
    QuickDialSite("Wikipedia", "https://www.wikipedia.org", Icons.Default.Public, Color(0xFF333333)),
    QuickDialSite("Stack Overflow", "https://stackoverflow.com", Icons.Default.Terminal, Color(0xFFF48024)),
    QuickDialSite("Detik News", "https://www.detik.com", Icons.Default.TravelExplore, Color(0xFF005691)),
    QuickDialSite("Kompas", "https://www.kompas.com", Icons.Default.Language, Color(0xFFD91D24))
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BrowserHomeScreen(
    state: BrowserUiState,
    onNavigateUrl: (String) -> Unit,
    onToggleIncognito: () -> Unit = {},
    onOpenQrDialog: () -> Unit = {},
    onOpenSearchEngineDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchText by remember { mutableStateOf("") }
    var showVoiceDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        // Spacer top
        item {
            Spacer(modifier = Modifier.height(28.dp))
        }

        // 1. Chrome-Style Nimbus Logo Wordmark
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                NimbusChromeLogo()
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "V3 SEARCH ENGINE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // 2. Large Pill Search Container (Chrome/Google style)
        item {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh ?: MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_search_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Google / Nimbus 'G' styled search icon
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF4285F4).copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Cari",
                                tint = Color(0xFF4285F4),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // TextField Input
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = {
                            Text(
                                text = "Telusuri ${state.searchEngine.displayName} atau ketik URL...",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (searchText.isNotBlank()) {
                                    onNavigateUrl(searchText)
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    // Right Icons: Mic & Camera Lens
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Mic icon
                        IconButton(
                            onClick = { showVoiceDialog = true },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Pencarian Suara",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Camera/Lens icon
                        IconButton(
                            onClick = onOpenQrDialog,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Pindai Kode / Kamera",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Quick Action Pill Buttons: "Mode AI" & "Samaran"
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mode AI Pill
                Surface(
                    onClick = {
                        onNavigateUrl("https://gemini.google.com")
                    },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Mode AI",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mode AI",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                // Samaran / Incognito Pill
                val isIncognitoActive = state.isIncognito
                Surface(
                    onClick = onToggleIncognito,
                    shape = CircleShape,
                    color = if (isIncognitoActive) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Samaran",
                            tint = if (isIncognitoActive) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isIncognitoActive) "Samaran Aktif" else "Samaran",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isIncognitoActive) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        // 4. Speed Dial Shortcuts Grid
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Situs Sering Dikunjungi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${defaultQuickDialSites.size} Pintasan",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    defaultQuickDialSites.chunked(4).forEach { rowSites ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowSites.forEach { site ->
                                ChromeQuickDialTile(
                                    site = site,
                                    onClick = { onNavigateUrl(site.url) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(4 - rowSites.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // 5. Minimalist Protection & Engine Switcher Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow ?: MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Keamanan & Mesin Telusur",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            onClick = onOpenSearchEngineDialog,
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = state.searchEngine.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ProtectionBadge(
                            icon = Icons.Default.Shield,
                            label = if (state.isAdBlockEnabled) "AdBlocker On" else "AdBlocker Off",
                            active = state.isAdBlockEnabled
                        )
                        ProtectionBadge(
                            icon = Icons.Default.Bolt,
                            label = if (state.isTurboModeEnabled) "GPU Turbo On" else "GPU Standar",
                            active = state.isTurboModeEnabled
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // Voice Search Dialog
    if (showVoiceDialog) {
        VoiceSearchModal(
            onDismiss = { showVoiceDialog = false },
            onVoiceQuerySubmitted = { query ->
                showVoiceDialog = false
                if (query.isNotBlank()) {
                    onNavigateUrl(query)
                }
            }
        )
    }
}

/**
 * Renders "Nimbus" in Google-inspired colorful display typography.
 */
@Composable
private fun NimbusChromeLogo() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        val letterColors = listOf(
            Color(0xFF4285F4), // N - Blue
            Color(0xFFEA4335), // i - Red
            Color(0xFFFBBC05), // m - Yellow
            Color(0xFF4285F4), // b - Blue
            Color(0xFF34A853), // u - Green
            Color(0xFFEA4335)  // s - Red
        )
        val letters = listOf("N", "i", "m", "b", "u", "s")

        letters.forEachIndexed { index, char ->
            Text(
                text = char,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = letterColors[index]
            )
        }
    }
}

@Composable
private fun ChromeQuickDialTile(
    site: QuickDialSite,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = site.badgeColor.copy(alpha = 0.12f),
            modifier = Modifier.size(54.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = site.icon,
                    contentDescription = site.title,
                    tint = site.badgeColor,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = site.title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProtectionBadge(
    icon: ImageVector,
    label: String,
    active: Boolean
) {
    Surface(
        shape = CircleShape,
        color = if (active) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VoiceSearchModal(
    onDismiss: () -> Unit,
    onVoiceQuerySubmitted: (String) -> Unit
) {
    var queryText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Mikrofon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "Pencarian Suara",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Bicara atau ketik pencarian Anda:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = queryText,
                    onValueChange = { queryText = it },
                    placeholder = { Text("misal: Berita terkini hari ini", fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = { onVoiceQuerySubmitted("Berita Terkini") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Berita", fontSize = 11.sp)
                    }
                    TextButton(
                        onClick = { onVoiceQuerySubmitted("Cuaca hari ini") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cuaca", fontSize = 11.sp)
                    }
                    TextButton(
                        onClick = { onVoiceQuerySubmitted("Hasil Sepak Bola") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Bola", fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onVoiceQuerySubmitted(queryText) }
            ) {
                Text("Cari", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
