package com.example.wearmusic.plugin

import android.content.Context
import android.webkit.WebView
import com.example.wearmusic.data.model.SearchResult
import com.example.wearmusic.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest

/**
 * 插件导入器
 * 支持通过 URL 链接导入 JS 插件
 */
class PluginImporter(
    private val context: Context,
    private val client: OkHttpClient
) {
    companion object {
        const val PLUGIN_DIR = "plugins"
    }

    private val pluginDir: File
        get() = File(context.filesDir, PLUGIN_DIR).apply { mkdirs() }

    /**
     * 从 URL 导入插件
     * @param url 插件 JS 文件的 URL
     * @return 导入的插件信息
     */
    suspend fun importFromUrl(url: String): Result<ImportedPlugin> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("下载失败: HTTP ${response.code}"))
            }
            
            val jsCode = response.body?.string() ?: return@withContext Result.failure(
                Exception("空响应体")
            )
            
            // 验证是否是有效的插件代码（简单检查）
            if (!jsCode.contains("plugin") && !jsCode.contains("search")) {
                return@withContext Result.failure(Exception("无效的插件格式"))
            }
            
            // 计算文件名（基于 URL 的 MD5）
            val fileName = md5(url) + ".js"
            val pluginFile = File(pluginDir, fileName)
            
            // 保存到本地
            pluginFile.writeText(jsCode)
            
            // 解析插件元数据
            val pluginInfo = parsePluginInfo(jsCode, url)
            
            Result.success(ImportedPlugin(
                fileName = fileName,
                url = url,
                name = pluginInfo.name,
                version = pluginInfo.version,
                localPath = pluginFile.absolutePath
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 加载本地已导入的插件
     */
    fun loadLocalPlugins(): List<ImportedPlugin> {
        return pluginDir.listFiles { _, name -> name.endsWith(".js") }?.map { file ->
            val jsCode = file.readText()
            val info = parsePluginInfo(jsCode, "")
            ImportedPlugin(
                fileName = file.name,
                url = "",
                name = info.name,
                version = info.version,
                localPath = file.absolutePath
            )
        } ?: emptyList()
    }

    /**
     * 删除插件
     */
    fun deletePlugin(fileName: String): Boolean {
        return File(pluginDir, fileName).delete()
    }

    /**
     * 解析插件基本信息
     */
    private fun parsePluginInfo(jsCode: String, url: String): PluginInfo {
        // 简单正则提取
        val nameMatch = Regex("name\\s*[:=]\\s*['\"]([^'\"]+)['\"]").find(jsCode)
        val versionMatch = Regex("version\\s*[:=]\\s*['\"]([^'\"]+)['\"]").find(jsCode)
        
        return PluginInfo(
            name = nameMatch?.groupValues?.get(1) ?: "未知插件",
            version = versionMatch?.groupValues?.get(1) ?: "1.0.0"
        )
    }

    private fun md5(input: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val bytes = digest.digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    data class ImportedPlugin(
        val fileName: String,
        val url: String,
        val name: String,
        val version: String,
        val localPath: String
    )

    private data class PluginInfo(val name: String, val version: String)
}
