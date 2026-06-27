package com.example.wearmusic.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TextField
import com.example.wearmusic.plugin.PluginImporter
import kotlinx.coroutines.launch

/**
 * 插件导入对话框组件
 * 支持通过 URL 导入 JS 插件
 */
@Composable
fun PluginImportDialog(
    pluginImporter: PluginImporter,
    onImportSuccess: (PluginImporter.ImportedPlugin) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    var url by remember { mutableStateOf("") }
    var isImporting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Card(
        onClick = { },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            Text(
                text = "导入插件",
                style = MaterialTheme.typography.title3,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "输入插件 URL",
                style = MaterialTheme.typography.caption1,
                modifier = Modifier.padding(top = 4.dp)
            )

            // URL 输入框
            TextField(
                value = url,
                onValueChange = { 
                    url = it
                    error = null
                    success = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("https://example.com/plugin.js") }
            )

            // 错误提示
            error?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.caption2,
                    color = androidx.compose.ui.graphics.Color.Red,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // 成功提示
            success?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.caption2,
                    color = androidx.compose.ui.graphics.Color.Green,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // 导入按钮
            Button(
                onClick = {
                    if (url.isEmpty()) {
                        error = "请输入 URL"
                        return@Button
                    }
                    isImporting = true
                    error = null
                    success = null

                    scope.launch {
                        val result = pluginImporter.importFromUrl(url)
                        isImporting = false

                        result.onSuccess { plugin ->
                            success = "导入成功: ${plugin.name}"
                            onImportSuccess(plugin)
                        }.onFailure { e ->
                            error = "导入失败: ${e.message}"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                enabled = !isImporting
            ) {
                Text(if (isImporting) "导入中..." else "导入")
            }

            // 取消按钮
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("取消")
            }
        }
    }
}
