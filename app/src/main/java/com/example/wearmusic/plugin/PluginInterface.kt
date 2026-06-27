package com.example.wearmusic.plugin

import com.example.wearmusic.data.model.Playlist
import com.example.wearmusic.data.model.SearchResult
import com.example.wearmusic.data.model.Song

/**
 * MusicFree 风格插件接口
 * 插件通过实现此接口提供音乐源
 */
interface MusicPlugin {

    /** 插件唯一标识 */
    val pluginId: 
String

    /** 插件名称 */
    val pluginName: String

    /** 插件版本 */
    val version: String

    /** 搜索歌曲 */
    suspend fun search(keyword: String, page: Int = 1, limit: Int = 20): SearchResult

    /** 获取歌曲详情（含真实播放链接） */
    suspend fun getSongDetail(song: Song): Song

    /** 获取歌词 */
    suspend fun getLyrics(song: Song): String

    /** 获取歌单内歌曲 */
    suspend fun getPlaylistSongs(playlist: Playlist, page: Int = 1): List<Song>

    /** 获取推荐歌单 */
    suspend fun getRecommendPlaylists(): List<Playlist>
}

/**
 * 插件管理器：动态加载和管理多个插件
 */
interface PluginManager {
    fun registerPlugin(plugin: MusicPlugin)
    fun unregisterPlugin(pluginId: String)
    fun getPlugin(pluginId: String): MusicPlugin?
    fun getAllPlugins(): List<MusicPlugin>
}
