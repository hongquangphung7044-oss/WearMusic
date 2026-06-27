package com.example.wearmusic.ui.screen

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.wearmusic.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class DownloadedSong(
    val title: String,
    val artist: String,
    val album: String,
    val filePath: String,
    val fileSize: Long
)

@Composable
fun DownloadsScreen(onPlaySong: (Song) -> Unit = {}) {
    val context = LocalContext.current
    var downloads by remember { mutableStateOf(listOf<DownloadedSong>()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    if (isLoading) {
        scope.launch {
            downloads = scanDownloads(context)
            isLoading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "下载管理",
            style = MaterialTheme.typography.title1,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )

        if (isLoading) {
            CircularProgressIndicator()
        } else if (downloads.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("暂无下载内容", style = MaterialTheme.typography.body2)
                Text("搜索歌曲后可下载", style = MaterialTheme.typography.caption1)
            }
        } else {
            Text(
                text = "共 ${downloads.size} 首",
                style = MaterialTheme.typography.caption1,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                downloads.forEach { item ->
                    Card(
                        onClick = {
                            onPlaySong(Song(
                                id = item.filePath,
                                title = item.title,
                                artist = item.artist,
                                album = item.album,
                                localPath = item.filePath
                            ))
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.body1,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${item.artist}  ${formatSize(item.fileSize)}",
                                style = MaterialTheme.typography.caption2
                            )
                        }
                    }
                }
            }
        }
    }
}

private suspend fun scanDownloads(context: Context): List<DownloadedSong> = withContext(Dispatchers.IO) {
    val musicDir = context.getExternalFilesDir("music") ?: return@withContext emptyList()
    val songs = mutableListOf<DownloadedSong>()

    musicDir.listFiles()?.forEach { songDir ->
        if (songDir.isDirectory) {
            val metadataFile = File(songDir, "metadata.json")
            val mp3Files = songDir.listFiles { f -> f.name.endsWith(".mp3") }

            if (mp3Files != null && mp3Files.isNotEmpty()) {
                val mp3 = mp3Files.first()
                val metadata = if (metadataFile.exists()) {
                    try {
                        org.json.JSONObject(metadataFile.readText())
                    } catch (_: Exception) { null }
                } else null

                songs.add(DownloadedSong(
                    title = metadata?.optString("title") ?: mp3.nameWithoutExtension,
                    artist = metadata?.optString("artist") ?: "未知",
                    album = metadata?.optString("album") ?: "",
                    filePath = mp3.absolutePath,
                    fileSize = mp3.length()
                ))
            }
        }
    }
    songs.sortedByDescending { it.filePath }
}

private fun formatSize(bytes: Long): String {
    val mb = bytes / (1024.0 * 1024.0)
    return if (mb >= 1) "%.1f MB".format(mb) else "${bytes / 1024} KB"
}
