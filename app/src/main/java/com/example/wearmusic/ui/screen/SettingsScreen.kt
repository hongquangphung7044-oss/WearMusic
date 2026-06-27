package com.example.wearmusic.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberScalingLazyListState
import com.example.wearmusic.plugin.PluginRepository
import com.example.wearmusic.ui.component.PluginImportDialog

enum class Quality(val label: String, val bitrate: Int) { STANDARD("标准", 128), HIGH("高品质", 320), LOSSLESS("无损", 999) }

@Composable
fun SettingsScreen(pluginRepository: PluginRepository, onQualityChange: (Quality) -> Unit = {}, onClearCache: () -> Unit = {}) {
    var showImportDialog by remember { mutableStateOf(false) }
    val listState = rememberScalingLazyListState()
    val plugins = remember { pluginRepository.getInstalledPlugins() }

    ScalingLazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
        item { ListHeader { Text("设置") } }
        item { Text("音质选择", style = MaterialTheme.typography.body1) }
        Quality.values().forEach { q -> item { Button(onClick = { onQualityChange(q) }, modifier = Modifier.fillMaxWidth()) { Text(q.label) } } }
        item { Button(onClick = { showImportDialog = true }, modifier = Modifier.fillMaxWidth()) { Text("导入插件") } }
        item { Button(onClick = onClearCache, modifier = Modifier.fillMaxWidth()) { Text("清除缓存") } }
        if (plugins.isNotEmpty()) {
            item { ListHeader { Text("已安装插件") } }
            plugins.forEach { p -> item { Text("${p.name} v${p.version}", style = MaterialTheme.typography.caption1) } }
        }
    }
    if (showImportDialog) PluginImportDialog(pluginRepository, { showImportDialog = false }, { showImportDialog = false })
}
