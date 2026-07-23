package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "whitelisted_sites")
data class WhitelistedSite(
    @PrimaryKey val domain: String,
    val addedAt: Long = System.currentTimeMillis(),
    val note: String = "User Whitelisted"
)

@Entity(tableName = "blocked_stat_entries")
data class BlockedStatEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val domain: String,
    val blockedUrl: String,
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String, // e.g. "2026-07-22"
    val monthString: String // e.g. "2026-07"
)

@Entity(tableName = "history_entries")
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmark_entries")
data class BookmarkEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "security_alerts")
data class SecurityAlert(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)
