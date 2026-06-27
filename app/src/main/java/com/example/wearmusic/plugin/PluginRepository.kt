package com.example.wearmusic.plugin

import android.content.Context
import android.util.Log
import android.webkit.WebView
import com.example.wearmusic.data.model.Playlist
import com.example.wearmusic.data.model.SearchResult
import com.example.wearmusic.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 插件仓库：管理已安装插件，提供搜索等操作
 */
class PluginRepository(
    private val context: Context,
    private val pluginImporter: PluginImporter
) {
    private var engine: JsPluginEngine? = null

    fun getInstalledPlugins(): List<PluginImporter.ImportedPlugin> {
        return pluginImporter.loadLocalPlugins()
    }

    suspend fun importPlugin(url: String): Result<PluginImporter.ImportedPlugin> {
        return pluginImporter.importFromUrl(url)
    }

    fun deletePlugin(fileName: String) {
        pluginImporter.deletePlugin(fileName)
    }

    suspend fun search(keyword: String): List<Song> = withContext(Dispatchers.IO) {
        val plugins = getInstalledPlugins()
        if (plugins.isEmpty()) return@withContext emptyList()

        // 确保 WebView 在主线程初始化
        val eng = engine ?: withContext(Dispatchers.Main) {
            val wv = WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
            }
            JsPluginEngine(wv).also { engine = it }
        }

        val allSongs = mutableListOf<Song>()
        for (plugin in plugins) {
            try {
                val jsCode = File(plugin.localPath).readText()
                // 加载插件
                withContext(Dispatchers.Main) { eng.loadPlugin(jsCode) }
                // 搜索
                val result = eng.search(keyword)
                allSongs.addAll(result.songs)
                Log.d("PluginRepo", "Plugin ${plugin.name}: found ${result.songs.size} songs")
            } catch (e: Exception) {
                Log.e("PluginRepo", "Plugin ${plugin.name} error: ${e.message}")
            }
        }
        allSongs
    }
}
