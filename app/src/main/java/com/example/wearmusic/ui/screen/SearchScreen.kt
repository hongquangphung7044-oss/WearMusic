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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.wearmusic.data.model.Song

@Composable
fun SearchScreen(
    onSongSelected: (Song) -> Unit = {},
    onDownload: (Song) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "搜索音乐",
            style = MaterialTheme.typography.title1,
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            text = if (searchQuery.isEmpty()) "选择搜索词" else "搜索: $searchQuery",
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("热门", "新歌", "英文", "日韩").forEach { keyword ->
                Button(
                    onClick = { searchQuery = keyword },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(keyword)
                }
            }
        }

        if (searchQuery.isNotEmpty()) {
            Button(
                onClick = { searchQuery = "" },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text("重新搜索")
            }

            Text(
                text = "搜索 \"$searchQuery\" 中...",
                style = MaterialTheme.typography.caption1,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
