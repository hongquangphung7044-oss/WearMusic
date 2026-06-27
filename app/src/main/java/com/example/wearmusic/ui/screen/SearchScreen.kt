package com.example.wearmusic.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.wearmusic.data.model.Song
import com.example.wearmusic.plugin.PluginRepository
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    pluginRepository: PluginRepository,
    onSongSelected: (Song) -> Unit = {},
    onDownload: (Song) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf(listOf<Song>()) }
    var isSearching by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val installedPlugins = remember { pluginRepository.getInstalledPlugins() }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "搜索音乐",
            style = MaterialTheme.typography.title1,
            modifier = Modifier.padding(top = 8.dp)
        )

        if (installedPlugins.isEmpty()) {
            Text(
                text = "请先在设置中导入插件",
                style = MaterialTheme.typography.caption1,
                color = androidx.compose.ui.graphics.Color.Red,
                modifier = Modifier.padding(8.dp),
                textAlign = TextAlign.Center
            )
        }

        Text(
            text = if (searchQuery.isEmpty()) "选择搜索词" else "搜索: $searchQuery",
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        if (searchQuery.isNotEmpty() && isSearching) {
            Text(
                text = "搜索中...",
                style = MaterialTheme.typography.caption1,
                modifier = Modifier.padding(4.dp)
            )
        }

        if (statusMessage.isNotEmpty()) {
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.caption2,
                modifier = Modifier.padding(4.dp),
                textAlign = TextAlign.Center
            )
        }

        // 预设搜索词
        if (searchQuery.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("热门歌曲", "轻音乐", "中文歌", "英文歌", "日韩").forEach { keyword ->
                    Button(
                        onClick = {
                            searchQuery = keyword
                            isSearching = true
                            statusMessage = ""
                            scope.launch {
                                try {
                                    val results = pluginRepository.search(keyword)
                                    searchResults = results
                                    statusMessage = if (results.isEmpty()) "未找到结果" else "找到 ${results.size} 首"
                                } catch (e: Exception) {
                                    statusMessage = "搜索失败: ${e.message}"
                                }
                                isSearching = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSearching && installedPlugins.isNotEmpty()
                    ) {
                        Text(keyword)
                    }
                }
            }
        } else {
            Button(
                onClick = { searchQuery = ""; searchResults = emptyList(); statusMessage = "" },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text("重新搜索")
            }
        }

        // 搜索结果
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            searchResults.take(8).forEach { song ->
                SongItem(song = song, onPlay = {
                    onSongSelected(song)
                }, onDownload = {
                    onDownload(song)
                })
            }
        }
    }
}

@Composable
private fun SongItem(song: Song, onPlay: () -> Unit, onDownload: () -> Unit) {
    Card(
        onClick = onPlay,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.body1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.caption1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
