package com.example.wearmusic.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
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
import com.example.wearmusic.data.model.Song
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
            MaterialTheme {
                AppNavHost(playerViewModel, pluginRepository)
            }
        }
    }
}

@Composable
fun AppNavHost(viewModel: PlayerViewModel, pluginRepo: PluginRepository) {
    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(navController = navController, startDestination = "main") {
        composable("main") {
            Scaffold(
                vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
                timeText = { TimeText() }
            ) { HomePage(viewModel, navController) }
        }
        composable("search") {
            Scaffold(
                vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
                timeText = { TimeText() }
            ) { SearchPage(pluginRepo, viewModel, navController) }
        }
        composable("downloads") {
            Scaffold(
                vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
                timeText = { TimeText() }
            ) { DownloadsPage(viewModel, navController) }
        }
        composable("settings") {
            Scaffold(
                vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
                timeText = { TimeText() }
            ) { SettingsPage(pluginRepo) }
        }
    }
}

@Composable
fun HomePage(viewModel: PlayerViewModel, navController: androidx.wear.compose.navigation.WearNavigatorController) {
    val state by viewModel.playerState.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val listState = rememberScalingLazyListState()

    ScalingLazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
        item {
            Text(when (state) {
                is PlayerViewModel.PlayerState.Playing -> (state as PlayerViewModel.PlayerState.Playing).song.title
                is PlayerViewModel.PlayerState.Paused -> (state as PlayerViewModel.PlayerState.Paused).song.title
                else -> "WearMusic"
            }, style = MaterialTheme.typography.title1, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
        item {
            Text(when (state) {
                is PlayerViewModel.PlayerState.Playing -> (state as PlayerViewModel.PlayerState.Playing).song.artist
                is PlayerViewModel.PlayerState.Paused -> (state as PlayerViewModel.PlayerState.Paused).song.artist
                else -> "腕上音乐"
            }, style = MaterialTheme.typography.body1, color = MaterialTheme.colors.secondary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
        item { Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.body1, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { viewModel.previous() }) { Text("<<") }
                Button(onClick = { viewModel.togglePlayPause() }) { Text(if (isPlaying) "||" else ">") }
                Button(onClick = { viewModel.next() }) { Text(">>") }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { navController.navigate("search") }, modifier = Modifier.weight(1f)) { Text("搜索") }
                Button(onClick = { navController.navigate("downloads") }, modifier = Modifier.weight(1f)) { Text("下载") }
                Button(onClick = { navController.navigate("settings") }, modifier = Modifier.weight(1f)) { Text("设置") }
            }
        }
    }
}

@Composable
fun SearchPage(pluginRepo: PluginRepository, viewModel: PlayerViewModel, navController: androidx.wear.compose.navigation.WearNavigatorController) {
    SearchScreen(pluginRepo) { song -> viewModel.play(song); navController.navigate("main") }
}

@Composable
fun DownloadsPage(viewModel: PlayerViewModel, navController: androidx.wear.compose.navigation.WearNavigatorController) {
    DownloadsScreen { song -> viewModel.play(song); navController.navigate("main") }
}

@Composable
fun SettingsPage(pluginRepo: PluginRepository) {
    SettingsScreen(pluginRepo)
}
