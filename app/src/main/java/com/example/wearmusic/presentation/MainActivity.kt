package com.example.wearmusic.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.rememberScalingLazyListState
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.wearmusic.player.PlayerViewModel
import com.example.wearmusic.plugin.PluginRepository
import com.example.wearmusic.ui.screen.DownloadsScreen
import com.example.wearmusic.ui.screen.SearchScreen
import com.example.wearmusic.ui.screen.SettingsScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val playerViewModel: PlayerViewModel by viewModels()
    @Inject lateinit var pluginRepository: PluginRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme { AppNavHost(playerViewModel, pluginRepository) }
        }
    }
}

@Composable
fun AppNavHost(viewModel: PlayerViewModel, pluginRepo: PluginRepository) {
    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(navController = navController, startDestination = "main") {
        composable("main") {
            val nc = navController
            Scaffold(vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }, timeText = { TimeText() }) {
                HomePage(viewModel, { nc.navigate("search") }, { nc.navigate("downloads") }, { nc.navigate("settings") })
            }
        }
        composable("search") {
            val nc = navController
            Scaffold(vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }, timeText = { TimeText() }) {
                SearchPage(pluginRepo, viewModel) { nc.navigate("main") }
            }
        }
        composable("downloads") {
            val nc = navController
            Scaffold(vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }, timeText = { TimeText() }) {
                DownloadsPage(viewModel) { nc.navigate("main") }
            }
        }
        composable("settings") {
            Scaffold(vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }, timeText = { TimeText() }) {
                SettingsPage(pluginRepo)
            }
        }
    }
}

@Composable
fun HomePage(viewModel: PlayerViewModel, onSearch: () -> Unit, onDownloads: () -> Unit, onSettings: () -> Unit) {
    val state by viewModel.playerState.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val listState = rememberScalingLazyListState()

    ScalingLazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
        item {
            val title = when (state) {
                is PlayerViewModel.PlayerState.Playing -> (state as PlayerViewModel.PlayerState.Playing).song.title
                is PlayerViewModel.PlayerState.Paused -> (state as PlayerViewModel.PlayerState.Paused).song.title
                else -> "WearMusic"
            }
            val artist = when (state) {
                is PlayerViewModel.PlayerState.Playing -> (state as PlayerViewModel.PlayerState.Playing).song.artist
                is PlayerViewModel.PlayerState.Paused -> (state as PlayerViewModel.PlayerState.Paused).song.artist
                else -> "腕上音乐"
            }
            Text(title, style = MaterialTheme.typography.title2, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp))
            Text(artist, style = MaterialTheme.typography.body1, color = MaterialTheme.colors.secondary, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth())
        }
        item {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
                CircularProgress(progress = progress, modifier = Modifier.size(80.dp), color = MaterialTheme.colors.primary)
                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.caption1)
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { viewModel.previous() }, modifier = Modifier.size(36.dp)) { Text("<<") }
                Button(onClick = { viewModel.togglePlayPause() }, modifier = Modifier.size(48.dp)) { Text(if (isPlaying) "||" else ">") }
                Button(onClick = { viewModel.next() }, modifier = Modifier.size(36.dp)) { Text(">>") }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = onSearch, modifier = Modifier.weight(1f)) { Text("搜索") }
                Button(onClick = onDownloads, modifier = Modifier.weight(1f)) { Text("下载") }
                Button(onClick = onSettings, modifier = Modifier.weight(1f)) { Text("设置") }
            }
        }
    }
}

@Composable
fun CircularProgress(progress: Float, modifier: Modifier = Modifier, color: Color = Color.White) {
    Canvas(modifier = modifier) {
        val strokeWidth = 4.dp.toPx()
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
        drawArc(color = color.copy(alpha = 0.2f), startAngle = -90f, sweepAngle = 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
        drawArc(color = color, startAngle = -90f, sweepAngle = progress * 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
    }
}

@Composable
fun SearchPage(pluginRepo: PluginRepository, viewModel: PlayerViewModel, goHome: () -> Unit) {
    SearchScreen(pluginRepo) { song -> viewModel.play(song); goHome() }
}

@Composable
fun DownloadsPage(viewModel: PlayerViewModel, goHome: () -> Unit) {
    DownloadsScreen { song -> viewModel.play(song); goHome() }
}

@Composable
fun SettingsPage(pluginRepo: PluginRepository) {
    SettingsScreen(pluginRepo)
}
