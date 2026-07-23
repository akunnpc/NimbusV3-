package com.example.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NavigationTab
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Slate500

@Composable
fun BottomNavBar(
    selectedTab: NavigationTab,
    alertsCount: Int,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            val itemColors = NavigationBarItemDefaults.colors(
                selectedIconColor = Indigo600,
                selectedTextColor = Indigo600,
                indicatorColor = Indigo600.copy(alpha = 0.12f),
                unselectedIconColor = Slate500,
                unselectedTextColor = Slate500
            )

            NavigationBarItem(
                selected = selectedTab == NavigationTab.BROWSER,
                onClick = { onTabSelected(NavigationTab.BROWSER) },
                icon = { Icon(imageVector = Icons.Default.Web, contentDescription = "Browser") },
                label = { Text("Browser", fontSize = 11.sp, fontWeight = if (selectedTab == NavigationTab.BROWSER) FontWeight.Bold else FontWeight.Normal) },
                colors = itemColors,
                modifier = Modifier.testTag("nav_tab_browser")
            )

            NavigationBarItem(
                selected = selectedTab == NavigationTab.ANALYTICS,
                onClick = { onTabSelected(NavigationTab.ANALYTICS) },
                icon = { Icon(imageVector = Icons.Default.Assessment, contentDescription = "Analitik") },
                label = { Text("Analitik", fontSize = 11.sp, fontWeight = if (selectedTab == NavigationTab.ANALYTICS) FontWeight.Bold else FontWeight.Normal) },
                colors = itemColors,
                modifier = Modifier.testTag("nav_tab_analytics")
            )

            NavigationBarItem(
                selected = selectedTab == NavigationTab.WHITELIST,
                onClick = { onTabSelected(NavigationTab.WHITELIST) },
                icon = { Icon(imageVector = Icons.Default.Security, contentDescription = "Daftar Putih") },
                label = { Text("Whitelist", fontSize = 11.sp, fontWeight = if (selectedTab == NavigationTab.WHITELIST) FontWeight.Bold else FontWeight.Normal) },
                colors = itemColors,
                modifier = Modifier.testTag("nav_tab_whitelist")
            )

            NavigationBarItem(
                selected = selectedTab == NavigationTab.HISTORY,
                onClick = { onTabSelected(NavigationTab.HISTORY) },
                icon = { Icon(imageVector = Icons.Default.History, contentDescription = "Riwayat") },
                label = { Text("Riwayat", fontSize = 11.sp, fontWeight = if (selectedTab == NavigationTab.HISTORY) FontWeight.Bold else FontWeight.Normal) },
                colors = itemColors,
                modifier = Modifier.testTag("nav_tab_history")
            )

            NavigationBarItem(
                selected = selectedTab == NavigationTab.ALERTS,
                onClick = { onTabSelected(NavigationTab.ALERTS) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (alertsCount > 0) {
                                Badge(containerColor = MaterialTheme.colorScheme.error) {
                                    Text(text = if (alertsCount > 9) "9+" else alertsCount.toString(), color = Color.White)
                                }
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = "Alerts")
                    }
                },
                label = { Text("Alerts", fontSize = 11.sp, fontWeight = if (selectedTab == NavigationTab.ALERTS) FontWeight.Bold else FontWeight.Normal) },
                colors = itemColors,
                modifier = Modifier.testTag("nav_tab_alerts")
            )

            NavigationBarItem(
                selected = selectedTab == NavigationTab.API_DOCS,
                onClick = { onTabSelected(NavigationTab.API_DOCS) },
                icon = { Icon(imageVector = Icons.Default.Code, contentDescription = "Dokumentasi API") },
                label = { Text("Docs", fontSize = 11.sp, fontWeight = if (selectedTab == NavigationTab.API_DOCS) FontWeight.Bold else FontWeight.Normal) },
                colors = itemColors,
                modifier = Modifier.testTag("nav_tab_docs")
            )
        }
    }
}
