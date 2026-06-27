package com.example.wearmusic.data.model

import kotlinx.serialization.Serializable

/**
 * 歌曲数据模型
 */
@Serializable
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val duration: Long = 0,
    val coverUrl: String = "",
    val mediaUrl: String = "",        // 在线播放地址
    val localPath: String = "",        // 本地缓存/下载路径
    val lyrics: String = "",           // 歌词文本或路径
    val bitrate: Int = 320,            // 比特率 kbps
    val source: String = ""            // 来源插件标识
)

/**
 * 歌单
 */
@Serializable
data class Playlist(
    val id: String,
    val name: String,
    val description: String = "",
    val coverUrl: String = "",
    val songs: List<Song> = emptyList()
)

/**
 * 搜索建议/结果
 */
@Serializable
data class SearchResult(
    val songs: List<Song> = emptyList(),
    val playlists: List<Playlist> = emptyList()
)
