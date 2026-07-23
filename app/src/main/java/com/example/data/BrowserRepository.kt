package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BrowserRepository(private val dao: BrowserDao) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    val whitelistedSites: Flow<List<WhitelistedSite>> = dao.getAllWhitelisted()
    val totalBlockedCount: Flow<Int> = dao.getTotalBlockedCount()
    val dailySummaries: Flow<List<DailyBlockSummary>> = dao.getDailyBlockSummaries()
    val topBlockedDomains: Flow<List<DomainBlockSummary>> = dao.getTopBlockedDomains()
    val historyEntries: Flow<List<HistoryEntry>> = dao.getAllHistory()
    val bookmarkEntries: Flow<List<BookmarkEntry>> = dao.getAllBookmarks()
    val securityAlerts: Flow<List<SecurityAlert>> = dao.getAllSecurityAlerts()

    fun getTodayBlockedCount(): Flow<Int> {
        val todayStr = dateFormat.format(Date())
        return dao.getBlockedCountForDate(todayStr)
    }

    fun getCurrentMonthBlockedCount(): Flow<Int> {
        val monthStr = monthFormat.format(Date())
        return dao.getBlockedCountForMonth(monthStr)
    }

    suspend fun recordBlockedAd(domain: String, blockedUrl: String) {
        val now = Date()
        val dateStr = dateFormat.format(now)
        val monthStr = monthFormat.format(now)

        val entry = BlockedStatEntry(
            domain = domain,
            blockedUrl = blockedUrl,
            timestamp = now.time,
            dateString = dateStr,
            monthString = monthStr
        )
        dao.insertBlockedStat(entry)

        // Record security alert if suspicious tracking or popunder script
        if (blockedUrl.contains("popads") || blockedUrl.contains("popunder") || blockedUrl.contains("tracking")) {
            dao.insertSecurityAlert(
                SecurityAlert(
                    url = blockedUrl,
                    reason = "Suspicious network tracker blocked from $domain"
                )
            )
        }
    }

    suspend fun addWhitelisted(domain: String, note: String = "User Whitelisted") {
        dao.insertWhitelisted(WhitelistedSite(domain = domain, note = note))
    }

    suspend fun removeWhitelisted(domain: String) {
        dao.deleteWhitelisted(domain)
    }

    suspend fun addHistory(title: String, url: String) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            dao.insertHistory(HistoryEntry(title = title, url = url))
        }
    }

    suspend fun clearHistory() {
        dao.clearHistory()
    }

    suspend fun addBookmark(title: String, url: String) {
        val existing = dao.getBookmarkByUrl(url)
        if (existing == null) {
            dao.insertBookmark(BookmarkEntry(title = title, url = url))
        }
    }

    suspend fun removeBookmark(id: Long) {
        dao.deleteBookmarkById(id)
    }

    suspend fun clearAlerts() {
        dao.clearSecurityAlerts()
    }
}
