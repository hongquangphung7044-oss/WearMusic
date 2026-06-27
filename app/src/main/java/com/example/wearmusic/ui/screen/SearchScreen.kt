package com.example.wearmusic.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TextField
import com.example.wearmusic.data.model.Song
import com.example.wearmusic.data.remote.MusicSearchService
import kotlinx.coroutines.launch

/**
 * 搜索界面
 * 支持插件搜索 + 网络歌词/封面补充
 */
@Composable
fun SearchScreen(
    musicSearchService: MusicSearchService,
    onSongSelected: (Song) -> Unit = {},
    onDownload: (Song) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf(listOf<Song>()) }
    var isSearching by remember { mutableStateOf(false) }
    var isEnriching by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "搜索音乐",
            style = MaterialTheme.typography.title1,
            modifier = Modifier.padding(top = 8.dp)
        )

        // 搜索输入框
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            label = { Text("输入歌曲名") }
        )

        Button(
            onClick = {
                isSearching = true
                scope.launch {
                    // 实际搜索应由插件完成
                    // 这里演示基础框架
                    searchResults = emptyList() // 插件搜索结果
                    isSearching = false
                }
            },
            modifier = Modifier.padding(vertical = 4.dp),
            enabled = !isSearching && searchQuery.isNotEmpty()
        ) {
            Text(if (isSearching) "搜索中..." else "搜索")
        }

        // 搜索结果列表
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(searchResults) { song ->
                SongSearchItem(
                    song = song,
                    musicSearchService = musicSearchService,
                    onPlay = { onSongSelected(song) },
                    onDownload = { onDownload(song) }
                )
            }

            if (!isSearching && searchResults.isEmpty() && searchQuery.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "未找到结果",
                            style = MaterialTheme.typography.body2
                        )
                        Text(
                            text = "请导入音乐插件",
                            style = MaterialTheme.typography.caption1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SongSearchItem(
    song: Song,
    musicSearchService: MusicSearchService,
    onPlay: () -> Unit,
    onDownload: () -> Unit
) {
    Card(
        onClick = onPlay,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
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

            // 操作按钮行
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Button(onClick = onPlay, modifier = Modifier.weight(1f)) {
                    Text("▶")
                }
                Button(onClick = onDownload, modifier = Modifier.weight(1f)) {
                    Text("⬇")
                }
            }
        }
    }
}
