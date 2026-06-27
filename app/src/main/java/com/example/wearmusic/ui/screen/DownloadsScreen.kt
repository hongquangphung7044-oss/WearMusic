package com.example.wearmusic.ui.screen

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Chip
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScalingLazyColumn
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.Vignette
import androidx.wear.compose.material3.VignettePosition
import androidx.wear.compose.material3.rememberScalingLazyListState
import com.example.wearmusic.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class DownloadedSong(
    val title: String, val artist: String, val album: String,
    val filePath: String, val fileSize: Long
)

@Composable
fun DownloadsScreen(onPlaySong: (Song) -> Unit = {}) {
    val context = LocalContext.current
    var downloads by remember { mutableStateOf(listOf<DownloadedSong>()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val listState = rememberScalingLazyListState()

    if (isLoading) {
        scope.launch { downloads = scanDownloads(context); isLoading = false }
    }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
    ) {
        item { ListHeader { Text("下载管理") } }

        if (isLoading) {
            item { CircularProgressIndicator() }
        } else if (downloads.isEmpty()) {
            item { Text("暂无下载内容", style = MaterialTheme.typography.body2, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp)) }
            item { Text("搜索歌曲后可下载", style = MaterialTheme.typography.label2, textAlign = TextAlign.Center) }
        } else {
            item { Text("共 ${downloads.size} 首", style = MaterialTheme.typography.label2, modifier = Modifier.padding(vertical = 4.dp)) }
            downloads.forEach { item ->
                item {
                    Chip(
                        onClick = {
                            onPlaySong(Song(id = item.filePath, title = item.title, artist = item.artist, album = item.album, localPath = item.filePath))
                        },
                        label = { Text(item.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        secondaryLabel = { Text("${item.artist}  ${formatSize(item.fileSize)}") },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    )
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
                val metadata = if (metadataFile.exists()) try { org.json.JSONObject(metadataFile.readText()) } catch (_: Exception) { null } else null
                songs.add(DownloadedSong(
                    title = metadata?.optString("title") ?: mp3.nameWithoutExtension,
                    artist = metadata?.optString("artist") ?: "未知",
                    album = metadata?.optString("album") ?: "",
                    filePath = mp3.absolutePath, fileSize = mp3.length()
                ))
            }
        }
    }
    songs
}

private fun formatSize(bytes: Long): String {
    val mb = bytes / (1024.0 * 1024.0)
    return if (mb >= 1) "%.1f MB".format(mb) else "${bytes / 1024} KB"
}
