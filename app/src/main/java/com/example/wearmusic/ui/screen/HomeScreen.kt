package com.example.wearmusic.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

/**
 * 主界面
 * 提供搜索、歌单、下载管理等入口
 */
@Composable
fun HomeScreen(
    onSearchClick: () -> Unit = {},
    onPlaylistClick: () -> Unit = {},
    onDownloadsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = "WearMusic",
            style = MaterialTheme.typography.title1,
            textAlign = TextAlign.Center
        )

        Text(
            text = "你的腕上音乐伴侣",
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = onSearchClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🔍 搜索音乐")
        }

        Button(
            onClick = onPlaylistClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🎵 我的歌单")
        }

        Button(
            onClick = onDownloadsClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("⬇ 下载管理")
        }
    }
}
