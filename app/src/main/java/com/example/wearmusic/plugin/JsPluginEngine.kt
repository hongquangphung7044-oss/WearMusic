package com.example.wearmusic.plugin

import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.example.wearmusic.data.model.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * JavaScript 插件引擎
 * 通过 WebView 桥接执行 JS 插件代码，支持 MusicFree 插件格式
 */
class JsPluginEngine(private val webView: WebView) {

    private val json = Json { ignoreUnknownKeys = true }

    init {
        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(PluginBridge(), "WearMusicBridge")
    }

    /**
     * 加载并执行插件 JS 文件
     */
    suspend fun loadPlugin(jsCode: String) = withContext(Dispatchers.Main) {
        webView.evaluateJavascript(jsCode, null)
    }

    /**
     * 调用插件搜索方法（异步回调方式）
     */
    suspend fun search(keyword: String, page: Int = 1, limit: Int = 20): SearchResult {
        val jsCode = "WearMusic.search('$keyword', $page, $limit)"
        val result = suspendCoroutine<String> { cont ->
            webView.evaluateJavascript(jsCode) { value ->
                cont.resume(value ?: "{\"songs\":[],\"playlists\":[]}")
            }
        }
        return json.decodeFromString(result)
    }

    /**
     * WebView JavaScript 桥接类
     */
    inner class PluginBridge {
        @JavascriptInterface
        fun log(message: String) {
            android.util.Log.d("JsPlugin", message)
        }
    }
}
