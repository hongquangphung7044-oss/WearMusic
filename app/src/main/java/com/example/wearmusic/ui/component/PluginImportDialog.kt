package com.example.wearmusic.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberScalingLazyListState
import com.example.wearmusic.plugin.PluginRepository
import kotlinx.coroutines.launch

private val PRESET_PLUGINS = listOf(
    "酷狗音乐" to "http://music.haitangw.net/cqapi/kg.js",
    "酷我音乐" to "http://music.haitangw.net/cqapi/kw.js",
    "资源综合" to "https://gitee.com/kevinr/tvbox/raw/master/musicfree/plugins/zz.js"
)

@Composable
fun PluginImportDialog(pluginRepository: PluginRepository, onImportSuccess: () -> Unit = {}, onDismiss: () -> Unit = {}) {
    var statusMessage by remember { mutableStateOf("") }
    var isImporting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberScalingLazyListState()

    ScalingLazyColumn(modifier = Modifier.fillMaxWidth().padding(8.dp), state = listState) {
        item { ListHeader { Text("导入音乐插件") } }
        item { Text("选择插件来源", style = MaterialTheme.typography.body1, modifier = Modifier.padding(bottom = 8.dp), textAlign = TextAlign.Center) }
        PRESET_PLUGINS.forEach { (name, url) ->
            item {
                Button(onClick = {
                    isImporting = true; statusMessage = "正在导入 $name..."
                    scope.launch {
                        pluginRepository.importPlugin(url).onSuccess { p ->
                            statusMessage = "导入成功: ${p.name}"; onImportSuccess()
                        }.onFailure { e -> statusMessage = "失败: ${e.message}" }
                        isImporting = false
                    }
                }, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), enabled = !isImporting) { Text(name) }
            }
        }
        if (statusMessage.isNotEmpty()) item {
            Text(statusMessage, style = MaterialTheme.typography.caption1, modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center, color = if (statusMessage.contains("成功")) Color.Green else Color.White)
        }
        item { Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("返回") } }
    }
}
