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
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberScalingLazyListState
import com.example.wearmusic.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class DownloadedSong(val title: String, val artist: String, val album: String, val filePath: String, val fileSize: Long)

@Composable
fun DownloadsScreen(onPlaySong: (Song) -> Unit = {}) {
    val context = LocalContext.current
    var downloads by remember { mutableStateOf(listOf<DownloadedSong>()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val listState = rememberScalingLazyListState()
    if (isLoading) { scope.launch { downloads = scanDownloads(context); isLoading = false } }

    ScalingLazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
        item { ListHeader { Text("下载管理") } }
        if (isLoading) { item { CircularProgressIndicator() } }
        else if (downloads.isEmpty()) {
            item { Text("暂无下载内容", style = MaterialTheme.typography.body1, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp)) }
            item { Text("搜索歌曲后可下载", style = MaterialTheme.typography.caption1, textAlign = TextAlign.Center) }
        } else {
            item { Text("共 ${downloads.size} 首", style = MaterialTheme.typography.caption1, modifier = Modifier.padding(vertical = 4.dp)) }
            downloads.forEach { d ->
                item {
                    Chip(onClick = { onPlaySong(Song(id = d.filePath, title = d.title, artist = d.artist, album = d.album, localPath = d.filePath)) },
                        label = { Text(d.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        secondaryLabel = { Text("${d.artist}  ${formatSize(d.fileSize)}") },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp))
                }
            }
        }
    }
}

private suspend fun scanDownloads(context: Context): List<DownloadedSong> = withContext(Dispatchers.IO) {
    val musicDir = context.getExternalFilesDir("music") ?: return@withContext emptyList()
    val songs = mutableListOf<DownloadedSong>()
    musicDir.listFiles()?.forEach { sd ->
        if (sd.isDirectory) {
            val mf = File(sd, "metadata.json")
            val mp3s = sd.listFiles { f -> f.name.endsWith(".mp3") }
            if (mp3s != null && mp3s.isNotEmpty()) {
                val m = mp3s.first()
                val meta = if (mf.exists()) try { org.json.JSONObject(mf.readText()) } catch (_: Exception) { null } else null
                songs.add(DownloadedSong(meta?.optString("title") ?: m.nameWithoutExtension, meta?.optString("artist") ?: "未知", meta?.optString("album") ?: "", m.absolutePath, m.length()))
            }
        }
    }
    songs
}
private fun formatSize(bytes: Long): String { val mb = bytes / (1024.0 * 1024.0); return if (mb >= 1) "%.1f MB".format(mb) else "${bytes / 1024} KB" }
