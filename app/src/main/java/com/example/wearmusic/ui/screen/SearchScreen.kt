package com.example.wearmusic.ui.screen

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
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.rememberScalingLazyListState
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
    val listState = rememberScalingLazyListState()

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        vignette = { Vignette(position = VignettePosition.TopAndBottom) }
    ) {
        item { ListHeader { Text("搜索音乐") } }

        if (installedPlugins.isEmpty()) {
            item {
                Text(
                    text = "请先在设置中导入插件",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        item {
            Text(
                text = if (searchQuery.isEmpty()) "选择搜索词" else "搜索: $searchQuery",
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (isSearching) {
            item {
                Text("搜索中...", style = MaterialTheme.typography.label2, modifier = Modifier.padding(4.dp))
            }
        }
        if (statusMessage.isNotEmpty()) {
            item {
                Text(statusMessage, style = MaterialTheme.typography.label2, modifier = Modifier.padding(4.dp), textAlign = TextAlign.Center)
            }
        }

        if (searchQuery.isEmpty()) {
            item { ListHeader { Text("快捷搜索") } }
            listOf("热门歌曲", "轻音乐", "中文歌", "英文歌").forEach { keyword ->
                item {
                    Button(
                        onClick = {
                            searchQuery = keyword; isSearching = true; statusMessage = ""
                            scope.launch {
                                try {
                                    searchResults = pluginRepository.search(keyword)
                                    statusMessage = if (searchResults.isEmpty()) "未找到结果" else "找到 ${searchResults.size} 首"
                                } catch (e: Exception) {
                                    statusMessage = "搜索失败: ${e.message}"
                                }
                                isSearching = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSearching && installedPlugins.isNotEmpty()
                    ) { Text(keyword) }
                }
            }
        } else {
            item {
                Button(onClick = { searchQuery = ""; searchResults = emptyList(); statusMessage = "" }, modifier = Modifier.fillMaxWidth()) {
                    Text("重新搜索")
                }
            }
        }

        if (searchResults.isNotEmpty()) {
            item { ListHeader { Text("搜索结果 (${searchResults.size})") } }
            searchResults.take(8).forEach { song ->
                item {
                    Chip(
                        onClick = { onSongSelected(song) },
                        label = { Text(song.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        secondaryLabel = { Text(song.artist, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}
