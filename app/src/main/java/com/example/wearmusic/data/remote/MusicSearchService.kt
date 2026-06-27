package com.example.wearmusic.data.remote

import com.example.wearmusic.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

/**
 * 音乐搜索服务
 * 封装歌曲搜索、封面获取、歌词获取
 */
class MusicSearchService(
    private val client: OkHttpClient
) {
    private val lyricsService = LyricsService(client)
    private val coverService = CoverService(client)

    companion object {
        // 音乐搜索 API（可通过插件替换）
        private const val SEARCH_API = "https://music.163.com/weapi/search/get"
    }

    /**
     * 搜索歌曲（基础实现，实际由插件提供）
     */
    suspend fun searchSongs(keyword: String): List<Song> = withContext(Dispatchers.IO) {
        try {
            // 这里使用占位实现，实际搜索应由插件完成
            // 返回空列表，提示需要导入插件
            emptyList<Song>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 获取歌曲详情（含封面、歌词）
     * 通过网络搜索补充缺失信息
     */
    suspend fun enrichSongInfo(song: Song): EnrichedSongInfo = withContext(Dispatchers.IO) {
        val lyrics = lyricsService.searchLyrics(song.title, song.artist)
        val coverUrl = coverService.searchCover(song.title, song.artist)
        
        EnrichedSongInfo(
            song = song,
            lyrics = lyrics,
            coverUrl = coverUrl
        )
    }

    /**
     * 下载歌曲 + 歌词 + 封面的完整包
     */
    suspend fun downloadFullPackage(
        song: Song,
        targetDir: java.io.File
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            targetDir.mkdirs()
            
            // 获取补充信息
            val enriched = enrichSongInfo(song)
            
            // 下载封面
            if (enriched.coverUrl.isNotEmpty()) {
                val coverFile = java.io.File(targetDir, "cover.jpg")
                coverService.downloadCover(enriched.coverUrl, coverFile)
            }
            
            // 保存歌词
            if (enriched.lyrics.isNotEmpty()) {
                val lyricsFile = java.io.File(targetDir, "lyrics.lrc")
                lyricsFile.writeText(enriched.lyrics)
            }
            
            // 保存元数据
            val metadata = java.io.File(targetDir, "metadata.json")
            metadata.writeText(
                """{"id":"${song.id}","title":"${song.title}","artist":"${song.artist}","album":"${song.album}","cover":"cover.jpg","lyrics":"lyrics.lrc"}"""
            )
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    data class EnrichedSongInfo(
        val song: Song,
        val lyrics: String,
        val coverUrl: String
    )
}
