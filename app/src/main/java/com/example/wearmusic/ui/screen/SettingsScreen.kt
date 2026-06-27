package com.example.wearmusic.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.wearmusic.plugin.PluginRepository
import com.example.wearmusic.ui.component.PluginImportDialog

enum class Quality(val label: String, val bitrate: Int) {
    STANDARD("标准 (128k)", 128),
    HIGH("高品质 (320k)", 320),
    LOSSLESS("无损", 999)
}

@Composable
fun SettingsScreen(
    pluginRepository: PluginRepository,
    onQualityChange: (Quality) -> Unit = {},
    onClearCache: () -> Unit = {}
) {
    var showImportDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = "设置",
            style = MaterialTheme.typography.title1,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(text = "音质选择", style = MaterialTheme.typography.body2)

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Quality.values().forEach { quality ->
                Button(
                    onClick = { onQualityChange(quality) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(quality.label)
                }
            }
        }

        Button(
            onClick = { showImportDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("导入插件")
        }

        Button(
            onClick = onClearCache,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("清除缓存")
        }

        val plugins = remember { pluginRepository.getInstalledPlugins() }
        if (plugins.isNotEmpty()) {
            Text(
                text = "已安装 (${plugins.size}个)",
                style = MaterialTheme.typography.caption1,
                modifier = Modifier.padding(top = 8.dp)
            )
            plugins.forEach { p ->
                Text(
                    text = "${p.name} v${p.version}",
                    style = MaterialTheme.typography.caption2
                )
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
