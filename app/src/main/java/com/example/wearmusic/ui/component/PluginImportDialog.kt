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
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.wearmusic.plugin.PluginRepository
import kotlinx.coroutines.launch

private val PRESET_PLUGINS = listOf(
    "示例来源 1" to "https://raw.githubusercontent.com/hongquangphung7044-oss/WearMusic/main/app/src/main/assets/plugins/demo-plugin.js"
)

@Composable
fun PluginImportDialog(
    pluginRepository: PluginRepository,
    onImportSuccess: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    var statusMessage by remember { mutableStateOf("") }
    var isImporting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = "导入音乐插件",
            style = MaterialTheme.typography.title3,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "选择要导入的插件来源",
            style = MaterialTheme.typography.caption1,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
        )

        PRESET_PLUGINS.forEach { (name, url) ->
            Button(
                onClick = {
                    isImporting = true
                    statusMessage = "正在导入 $name..."
                    scope.launch {
                        val result = pluginRepository.importPlugin(url)
                        isImporting = false
                        result.onSuccess { plugin ->
                            statusMessage = "导入成功: ${plugin.name}"
                            onImportSuccess()
                        }.onFailure { e ->
                            statusMessage = "失败: ${e.message}"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                enabled = !isImporting
            ) {
                Text(name)
            }
        }

        if (statusMessage.isNotEmpty()) {
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.caption2,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center,
                color = if (statusMessage.contains("成功"))
                    androidx.compose.ui.graphics.Color.Green
                else
                    androidx.compose.ui.graphics.Color.White
            )
        }

        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("返回")
        }
    }
}
