package com.example.wearmusic.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.wearmusic.data.model.Song

/**
 * 下载管理界面
 * 显示已下载的歌曲列表
 */
@Composable
fun DownloadsScreen(
    downloads: List<DownloadItem> = emptyList()
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "下载管理",
            style = MaterialTheme.typography.title1,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(downloads) { item ->
                DownloadItemCard(item = item)
            }

            if (downloads.isEmpty()) {
                item {
                    Text(
                        text = "暂无下载内容",
                        style = MaterialTheme.typography.caption1,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

data class DownloadItem(
    val song: Song,
    val status: DownloadStatus,
    val progress: Float = 0f
)

enum class DownloadStatus {
    PENDING, DOWNLOADING, COMPLETED, FAILED, PAUSED
}

@Composable
fun DownloadItemCard(item: DownloadItem) {
    Card(
        onClick = { /* 播放或管理 */ },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = item.song.title,
                style = MaterialTheme.typography.body1
            )
            Text(
                text = when (item.status) {
                    DownloadStatus.PENDING -> "等待中"
                    DownloadStatus.DOWNLOADING -> "下载中 ${(item.progress * 100).toInt()}%"
                    DownloadStatus.COMPLETED -> "已完成"
                    DownloadStatus.FAILED -> "失败"
                    DownloadStatus.PAUSED -> "暂停"
                },
                style = MaterialTheme.typography.caption2
            )
        }
    }
}
