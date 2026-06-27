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
import com.example.wearmusic.plugin.PluginImporter
import com.example.wearmusic.ui.component.PluginImportDialog

/**
 * 设置界面
 * 音质选择、下载设置、清除缓存、插件导入
 */
@Composable
fun SettingsScreen(
    pluginImporter: PluginImporter,
    onQualityChange: (Quality) -> Unit = {},
    onClearCache: () -> Unit = {},
    onPluginImported: (PluginImporter.ImportedPlugin) -> Unit = {}
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

        Text(
            text = "音质选择",
            style = MaterialTheme.typography.body2
        )

        // 音质切换按钮
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

        // 插件导入按钮
        Button(
            onClick = { showImportDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📦 导入插件")
        }

        Button(
            onClick = onClearCache,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🧹 清除缓存")
        }

        // 显示已导入的插件列表
        val localPlugins = remember { pluginImporter.loadLocalPlugins() }
        if (localPlugins.isNotEmpty()) {
            Text(
                text = "已安装插件 (${localPlugins.size}个)",
                style = MaterialTheme.typography.caption1,
                modifier = Modifier.padding(top = 8.dp)
            )
            localPlugins.forEach { plugin ->
                Text(
                    text = "${plugin.name} v${plugin.version}",
                    style = MaterialTheme.typography.caption2
                )
            }
        }
    }

    // 插件导入对话框
    if (showImportDialog) {
        PluginImportDialog(
            pluginImporter = pluginImporter,
            onImportSuccess = { plugin ->
                onPluginImported(plugin)
                showImportDialog = false
            },
            onDismiss = { showImportDialog = false }
        )
    }
}

enum class Quality(val label: String, val bitrate: Int) {
    STANDARD("标准 (128k)", 128),
    HIGH("高品质 (320k)", 320),
    LOSSLESS("无损", 999)
}
