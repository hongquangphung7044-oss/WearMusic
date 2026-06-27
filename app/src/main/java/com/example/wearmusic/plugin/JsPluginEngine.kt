package com.example.wearmusic.plugin

import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.example.wearmusic.data.model.Playlist
import com.example.wearmusic.data.model.SearchResult
import com.example.wearmusic.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.json.JSONObject

/**
 * JavaScript 插件引擎
 * 通过 WebView 桥接执行 JS 插件代码，支持 MusicFree 插件格式
 */
class JsPluginEngine(private val webView: WebView) {

    private val json = Json { ignoreUnknownKeys = true }
    private var currentCallbacks = mutableMapOf<String, (String) -> Unit>()

    init {
        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(PluginBridge(), "WearMusicBridge")
    }

    /**
     * 加载并执行插件 JS 文件
     */
    suspend fun loadPlugin(jsCode: String) = withContext(Dispatchers.Main) {
        val wrappedJs = """
            $jsCode
            // 注册全局搜索接口
            window.WearMusic = {
                search: async function(keyword, page, limit) {
                    return await plugin.search(keyword, page, limit);
                },
                getMediaUrl: async function(song) {
                    return await plugin.getMediaUrl(song);
                },
                getLyrics: async function(song) {
                    return await plugin.getLyrics(song);
                }
            };
        """.trimIndent()
        webView.evaluateJavascript(wrappedJs, null)
    }

    /**
     * 调用插件搜索方法
     */
    suspend fun search(keyword: String, page: Int = 1, limit: Int = 20): SearchResult {
        return executeJs("WearMusic.search('$keyword', $page, $limit)")
    }

    /**
     * 通过 JS 桥接执行代码并返回结果
     */
    private suspend fun executeJs(jsCode: String): SearchResult {
        return withContext(Dispatchers.Main) {
            val result = webView.evaluateJavascript(jsCode) { value ->
                // 处理返回值
            }
            json.decodeFromString(result ?: "{\"songs\":[],\"playlists\":[]}")
        }
    }

    /**
     * WebView JavaScript 桥接类
     */
    inner class PluginBridge {
        @JavascriptInterface
        fun log(message: String) {
            android.util.Log.d("JsPlugin", message)
        }

        @JavascriptInterface
        fun returnResult(callbackId: String, result: String) {
            currentCallbacks[callbackId]?.invoke(result)
            currentCallbacks.remove(callbackId)
        }
    }
}
