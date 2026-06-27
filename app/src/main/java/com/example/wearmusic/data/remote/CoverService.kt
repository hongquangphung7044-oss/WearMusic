package com.example.wearmusic.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

/**
 * 封面搜索服务
 * 通过公开 API 搜索歌曲封面图片
 */
class CoverService(private val client: OkHttpClient) {

    companion object {
        // 封面搜索 API
        private const val LASTFM_API = "https://ws.audioscrobbler.com/2.0/?method=album.search&api_key=YOUR_API_KEY&format=json&album="
        private const val DEEZER_API = "https://api.deezer.com/search?q="
        private const val ITUNES_API = "https://itunes.apple.com/search?limit=1&entity=song&term="
    }

    /**
     * 搜索歌曲封面 URL
     * @param title 歌曲名
     * @param artist 歌手名
     * @return 封面图片 URL，找不到返回空字符串
     */
    suspend fun searchCover(title: String, artist: String = ""): String = withContext(Dispatchers.IO) {
        try {
            val query = if (artist.isNotEmpty()) "$title $artist" else title
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            
            // 优先使用 iTunes API（无需 API Key，稳定）
            val itunesResult = searchItunes(encodedQuery)
            if (itunesResult.isNotEmpty()) return@withContext itunesResult
            
            // 备用：Deezer
            val deezerResult = searchDeezer(encodedQuery)
            if (deezerResult.isNotEmpty()) return@withContext deezerResult
            
            // 最后尝试 Last.fm
            searchLastFm(encodedQuery)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private suspend fun searchItunes(query: String): String {
        return try {
            val request = Request.Builder()
                .url("$ITUNES_API$query")
                .build()
            
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return "" 
            
            if (!response.isSuccessful) return ""
            
            val json = JSONObject(body)
            if (json.has("results") && json.getJSONArray("results").length() > 0) {
                val track = json.getJSONArray("results").getJSONObject(0)
                // iTunes 封面的 artworkUrl100，可替换为高分辨率
                val artworkUrl = track.optString("artworkUrl100", "")
                // 替换为更高分辨率（可选）
                artworkUrl.replace("100x100", "600x600")
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    private suspend fun searchDeezer(query: String): String {
        return try {
            val request = Request.Builder()
                .url("$DEEZER_API$query")
                .build()
            
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return ""
            
            if (!response.isSuccessful) return ""
            
            val json = JSONObject(body)
            if (json.has("data")) {
                val tracks = json.getJSONArray("data")
                if (tracks.length() > 0) {
                    val album = tracks.getJSONObject(0).optJSONObject("album")
                    album?.optString("cover_big", "") ?: ""
                } else {
                    ""
                }
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    private suspend fun searchLastFm(query: String): String {
        // 需要 API Key，作为备用
        return "" 
    }

    /**
     * 下载封面图片到本地
     * @param coverUrl 封面 URL
     * @param targetFile 目标文件路径
     */
    suspend fun downloadCover(coverUrl: String, targetFile: java.io.File): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(coverUrl).build()
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) return@withContext false
            
            response.body?.bytes()?.let { bytes ->
                targetFile.parentFile?.mkdirs()
                targetFile.writeBytes(bytes)
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
