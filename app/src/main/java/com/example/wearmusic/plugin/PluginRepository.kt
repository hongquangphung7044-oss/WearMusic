package com.example.wearmusic.plugin

import android.content.Context
import android.webkit.WebView
import com.example.wearmusic.data.model.Playlist
import com.example.wearmusic.data.model.SearchResult
import com.example.wearmusic.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * 插件仓库：管理已安装插件，提供搜索等操作
 */
class PluginRepository(
    private val context: Context,
    private val pluginImporter: PluginImporter
) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    private val webView: WebView by lazy { createWebView() }
    private val engine: JsPluginEngine by lazy { JsPluginEngine(webView) }

    private val pluginsFile: File
        get() = File(context.filesDir, "plugins.json")

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
        val plugins = pluginImporter.loadLocalPlugins()
        if (plugins.isEmpty()) return@withContext emptyList()

        val allSongs = mutableListOf<Song>()
        for (plugin in plugins) {
            try {
                val jsCode = File(plugin.localPath).readText()
                engine.loadPlugin(jsCode)
                val result = engine.search(keyword, 1, 20)
                allSongs.addAll(result.songs)
            } catch (e: Exception) {
                android.util.Log.e("PluginRepo", "Plugin search error: ${e.message}")
            }
        }
        allSongs
    }

    private fun createWebView(): WebView {
        return WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
        }
    }
}
