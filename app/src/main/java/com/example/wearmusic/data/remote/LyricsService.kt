package com.example.wearmusic.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

/**
 * 歌词搜索服务
 * 聚合多个歌词源 API
 */
class LyricsService(private val client: OkHttpClient) {

    companion object {
        // 公开的代理 API（可根据实际情况替换）
        private const val LYRIC_API_1 = "https://lrclib.net/api/search?q="
        private const val LYRIC_API_2 = "https://music.liuzhijin.cn/api/"
        private const val LYRIC_API_3 = "https://api.lrc.cx/lyrics?id="
    }

    /**
     * 搜索歌词
     * @param title 歌曲名
     * @param artist 歌手（可选）
     * @return LRC 格式歌词文本
     */
    suspend fun searchLyrics(title: String, artist: String = ""): String = withContext(Dispatchers.IO) {
        try {
            // 优先使用 lrclib.net（免费且稳定）
            val query = if (artist.isNotEmpty()) "$title $artist" else title
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            
            val request = Request.Builder()
                .url("$LYRIC_API_1$encodedQuery")
                .build()
            
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext ""
            
            if (!response.isSuccessful) return@withContext ""
            
            // 解析 lrclib 响应
            val json = JSONObject(body)
            if (json.has("tracks")) {
                val tracks = json.getJSONArray("tracks")
                if (tracks.length() > 0) {
                    val firstTrack = tracks.getJSONObject(0)
                    if (firstTrack.has("syncedLyrics")) {
                        return@withContext firstTrack.getString("syncedLyrics")
                    }
                }
            }
            
            // 备用：尝试其他格式
            parseLyricsResponse(body)
        } catch (e: Exception) {
            e.printStackTrace()
            "[00:00.000]歌词加载失败"
        }
    }

    /**
     * 通过歌曲 ID 获取歌词（特定平台）
     */
    suspend fun getLyricsById(songId: String, platform: String = "netease"): String = withContext(Dispatchers.IO) {
        try {
            val url = when (platform) {
                "netease" -> "https://music.163.com/api/song/media?id=$songId"
                "qq" -> "https://c.y.qq.com/lyric/fcgi-bin/fcg_query_lyric_new.fcg?songmid=$songId"
                else -> return@withContext ""
            }
            
            val request = Request.Builder().url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()
            
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext ""
            
            parseLyricsResponse(body)
        } catch (e: Exception) {
            e.printStackTrace()
            "[00:00.000]歌词加载失败"
        }
    }

    /**
     * 解析不同格式的歌词响应
     */
    private fun parseLyricsResponse(response: String): String {
        return try {
            // 尝试 JSON 解析
            val json = JSONObject(response)
            when {
                json.has("lrc") -> {
                    // 网易格式
                    val lrcObj = json.getJSONObject("lrc")
                    lrcObj.optString("lyric", "")
                }
                json.has("lyric") -> {
                    json.getString("lyric")
                }
                else -> {
                    // 直接返回文本（LRC格式）
                    if (response.contains("[00:")) response else ""
                }
            }
        } catch (e: Exception) {
            // 非 JSON，尝试直接返回
            if (response.contains("[00:")) response else "[00:00.000]暂无歌词"
        }
    }

    /**
     * 解析 LRC 歌词为时间-文本映射
     */
    data class LyricLine(val timeMs: Long, val text: String)

    fun parseLrc(lrcText: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        val pattern = Regex("\\[(\\d{2}):([\\d\\.]+)\\](.*)")
        
        lrcText.lines().forEach { line ->
            val match = pattern.find(line.trim())
            if (match != null) {
                val minutes = match.groupValues[1].toLong()
                val seconds = match.groupValues[2].toDouble()
                val text = match.groupValues[3].trim()
                val timeMs = (minutes * 60 * 1000 + seconds * 1000).toLong()
                lines.add(LyricLine(timeMs, text))
            }
        }
        
        return lines.sortedBy { it.timeMs }
    }
}
