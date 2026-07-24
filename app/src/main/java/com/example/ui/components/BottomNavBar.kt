package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NavigationTab

@Composable
fun BottomNavBar(
    selectedTab: NavigationTab,
    alertsCount: Int,
    downloadsCount: Int = 0,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surfaceContainerHigh ?: MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItemBox(
                selected = selectedTab == NavigationTab.BROWSER,
                onClick = { onTabSelected(NavigationTab.BROWSER) },
                icon = Icons.Default.Web,
                contentDescription = "Browser",
                testTag = "nav_tab_browser"
            )

            NavItemBox(
                selected = selectedTab == NavigationTab.ANALYTICS,
                onClick = { onTabSelected(NavigationTab.ANALYTICS) },
                icon = Icons.Default.Assessment,
                contentDescription = "Analitik",
                testTag = "nav_tab_analytics"
            )

            NavItemBox(
                selected = selectedTab == NavigationTab.WHITELIST,
                onClick = { onTabSelected(NavigationTab.WHITELIST) },
                icon = Icons.Default.Security,
                contentDescription = "Daftar Putih",
                testTag = "nav_tab_whitelist"
            )

            NavItemBox(
                selected = selectedTab == NavigationTab.HISTORY,
                onClick = { onTabSelected(NavigationTab.HISTORY) },
                icon = Icons.Default.History,
                contentDescription = "Riwayat",
                testTag = "nav_tab_history"
            )

            NavItemBox(
                selected = selectedTab == NavigationTab.DOWNLOADS,
                onClick = { onTabSelected(NavigationTab.DOWNLOADS) },
                icon = Icons.Default.Download,
                badgeCount = downloadsCount,
                badgeColor = MaterialTheme.colorScheme.primary,
                contentDescription = "Unduhan",
                testTag = "nav_tab_downloads"
            )

            NavItemBox(
                selected = selectedTab == NavigationTab.ALERTS,
                onClick = { onTabSelected(NavigationTab.ALERTS) },
                icon = Icons.Default.NotificationsActive,
                badgeCount = alertsCount,
                badgeColor = MaterialTheme.colorScheme.error,
                contentDescription = "Alerts",
                testTag = "nav_tab_alerts"
            )

            NavItemBox(
                selected = selectedTab == NavigationTab.API_DOCS,
                onClick = { onTabSelected(NavigationTab.API_DOCS) },
                icon = Icons.Default.Code,
                contentDescription = "Dokumentasi API",
                testTag = "nav_tab_docs"
            )
        }
    }
}

@Composable
private fun NavItemBox(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    testTag: String,
    badgeCount: Int = 0,
    badgeColor: Color = MaterialTheme.colorScheme.primary
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }

    val iconColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    }

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        if (badgeCount > 0) {
            BadgedBox(
                badge = {
                    Badge(containerColor = badgeColor) {
                        Text(
                            text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                            color = Color.White,
                            fontSize = 9.sp
                        )
                    }
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        } else {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

