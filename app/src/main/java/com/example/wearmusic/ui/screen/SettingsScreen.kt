package com.example.wearmusic.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScalingLazyColumn
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.Vignette
import androidx.wear.compose.material3.VignettePosition
import androidx.wear.compose.material3.rememberScalingLazyListState
import com.example.wearmusic.plugin.PluginRepository
import com.example.wearmusic.ui.component.PluginImportDialog

enum class Quality(val label: String, val bitrate: Int) {
    STANDARD("标准", 128), HIGH("高品质", 320), LOSSLESS("无损", 999)
}

@Composable
fun SettingsScreen(
    pluginRepository: PluginRepository,
    onQualityChange: (Quality) -> Unit = {},
    onClearCache: () -> Unit = {}
) {
    var showImportDialog by remember { mutableStateOf(false) }
    val listState = rememberScalingLazyListState()

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
    ) {
        item { ListHeader { Text("设置") } }

        item { Text("音质选择", style = MaterialTheme.typography.body2) }
        Quality.values().forEach { quality ->
            item {
                Button(onClick = { onQualityChange(quality) }, modifier = Modifier.fillMaxWidth()) {
                    Text(quality.label)
                }
            }
        }

        item {
            Button(onClick = { showImportDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Text("导入插件")
            }
        }
        item {
            Button(onClick = onClearCache, modifier = Modifier.fillMaxWidth()) {
                Text("清除缓存")
            }
        }

        val plugins = remember { pluginRepository.getInstalledPlugins() }
        if (plugins.isNotEmpty()) {
            item { ListHeader { Text("已安装插件") } }
            plugins.forEach { p ->
                item { Text("${p.name} v${p.version}", style = MaterialTheme.typography.label2) }
            }
        }
    }

    if (showImportDialog) {
        PluginImportDialog(
            pluginRepository = pluginRepository,
            onImportSuccess = { showImportDialog = false },
            onDismiss = { showImportDialog = false }
        )
    }
}
