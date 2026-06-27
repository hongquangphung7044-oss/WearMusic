package com.example.wearmusic.plugin

import android.webkit.WebView
import com.example.wearmusic.data.model.SearchResult
import com.example.wearmusic.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * JavaScript 插件引擎
 * 支持 MusicFree 插件格式 (window.plugin)
 */
class JsPluginEngine(private val webView: WebView) {
    private val json = Json { ignoreUnknownKeys = true }

    init {
        webView.settings.javaScriptEnabled = true
    }

    /**
     * 加载插件 JS，包装成 window.plugin 格式
     */
    suspend fun loadPlugin(jsCode: String) = withContext(Dispatchers.Main) {
        // 先执行插件代码（它会把函数注册到 window.plugin 上）
        webView.evaluateJavascript(jsCode, null)
        // 等待一小段时间确保插件初始化完成
        kotlinx.coroutines.delay(500)
    }

    /**
     * 调用 plugin.search(keyword, page, limit)
     */
    suspend fun search(keyword: String, page: Int = 1, limit: Int = 20): SearchResult = withContext(Dispatchers.Main) {
        val safeKeyword = keyword.replace("'", "\\'").replace("\"", "\\\"")
        val jsCode = """
            (function() {
                if (typeof window.plugin === 'undefined') return JSON.stringify({songs:[],playlists:[]});
                try {
                    var result = window.plugin.search('$safeKeyword', $page, $limit);
                    if (result && typeof result.then === 'function') {
                        result.then(function(r) {
                            window._pluginResult = JSON.stringify(r);
                        }).catch(function(e) {
                            window._pluginResult = JSON.stringify({songs:[],playlists:[]});
                        });
                        return 'PENDING';
                    }
                    return JSON.stringify(result);
                } catch(e) {
                    return JSON.stringify({songs:[],playlists:[],error:e.message});
                }
            })();
        """.trimIndent()

        val raw = suspendCoroutine<String> { cont ->
            webView.evaluateJavascript(jsCode) { value -> cont.resume(value ?: "{}") }
        }

        // 如果是异步 Promise，需要等待回调
        if (raw.contains("\"PENDING\"")) {
            // 轮询等待结果
            var waited = 0
            var result = ""
            while (waited < 5000) {
                kotlinx.coroutines.delay(200)
                waited += 200
                result = suspendCoroutine { cont2 ->
                    webView.evaluateJavascript("window._pluginResult") { v -> cont2.resume(v ?: "null") }
                }
                if (result != "null" && result.isNotEmpty()) break
            }
            parseSearchResult(if (result.isNotEmpty()) result else "{}")
        } else {
            parseSearchResult(raw)
        }
    }

    private fun parseSearchResult(raw: String): SearchResult {
        val cleaned = raw.trim().removeSurrounding("\"").replace("\\\"", "\"")
        return try {
            json.decodeFromString<SearchResult>(cleaned)
        } catch (e: Exception) {
            android.util.Log.e("JsPlugin", "Parse error: ${e.message}, raw=$raw")
            SearchResult(emptyList(), emptyList())
        }
    }
}
