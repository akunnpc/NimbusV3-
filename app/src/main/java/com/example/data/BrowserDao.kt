package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class DailyBlockSummary(
    val dateString: String,
    val count: Int
)

data class DomainBlockSummary(
    val domain: String,
    val count: Int
)

@Dao
interface BrowserDao {

    // --- Whitelist ---
    @Query("SELECT * FROM whitelisted_sites ORDER BY addedAt DESC")
    fun getAllWhitelisted(): Flow<List<WhitelistedSite>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWhitelisted(site: WhitelistedSite)

    @Query("DELETE FROM whitelisted_sites WHERE domain = :domain")
    suspend fun deleteWhitelisted(domain: String)

    // --- Blocked Stats ---
    @Insert
    suspend fun insertBlockedStat(entry: BlockedStatEntry)

    @Query("SELECT COUNT(*) FROM blocked_stat_entries WHERE dateString = :dateString")
    fun getBlockedCountForDate(dateString: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM blocked_stat_entries")
    fun getTotalBlockedCount(): Flow<Int>

    @Query("SELECT dateString, COUNT(*) as count FROM blocked_stat_entries GROUP BY dateString ORDER BY dateString DESC LIMIT 7")
    fun getDailyBlockSummaries(): Flow<List<DailyBlockSummary>>

    @Query("SELECT domain, COUNT(*) as count FROM blocked_stat_entries GROUP BY domain ORDER BY count DESC LIMIT 5")
    fun getTopBlockedDomains(): Flow<List<DomainBlockSummary>>

    @Query("SELECT COUNT(*) FROM blocked_stat_entries WHERE monthString = :monthString")
    fun getBlockedCountForMonth(monthString: String): Flow<Int>

    // --- History ---
    @Query("SELECT * FROM history_entries ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entry: HistoryEntry)

    @Query("DELETE FROM history_entries")
    suspend fun clearHistory()

    @Query("DELETE FROM history_entries WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    // --- Bookmarks ---
    @Query("SELECT * FROM bookmark_entries ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(entry: BookmarkEntry)

    @Query("DELETE FROM bookmark_entries WHERE id = :id")
    suspend fun deleteBookmarkById(id: Long)

    @Query("SELECT * FROM bookmark_entries WHERE url = :url LIMIT 1")
    suspend fun getBookmarkByUrl(url: String): BookmarkEntry?

    // --- Security Alerts ---
    @Query("SELECT * FROM security_alerts ORDER BY timestamp DESC LIMIT 50")
    fun getAllSecurityAlerts(): Flow<List<SecurityAlert>>

    @Insert
    suspend fun insertSecurityAlert(alert: SecurityAlert)

    @Query("DELETE FROM security_alerts")
    suspend fun clearSecurityAlerts()
}
