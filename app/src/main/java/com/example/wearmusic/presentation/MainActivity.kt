package com.example.wearmusic.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScalingLazyColumn
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.Vignette
import androidx.wear.compose.material3.VignettePosition
import androidx.wear.compose.material3.items
import androidx.wear.compose.material3.rememberScalingLazyListState
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

    @Inject
    lateinit var pluginRepository: PluginRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                WearMusicNavHost(playerViewModel, pluginRepository)
            }
        }
    }
}

@Composable
fun WearMusicNavHost(viewModel: PlayerViewModel, pluginRepo: PluginRepository) {
    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(navController = navController, startDestination = "home") {
        composable("home") {
            PlayerScreen(
                playerViewModel = viewModel,
                onSearch = { navController.navigate("search") },
                onDownloads = { navController.navigate("downloads") },
                onSettings = { navController.navigate("settings") }
            )
        }
        composable("search") {
            SearchScreen(
                pluginRepository = pluginRepo,
                onSongSelected = { song -> viewModel.play(song); navController.navigate("home") }
            )
        }
        composable("downloads") {
            DownloadsScreen(onPlaySong = { song -> viewModel.play(song); navController.navigate("home") })
        }
        composable("settings") {
            SettingsScreen(pluginRepository = pluginRepo)
        }
    }
}

@Composable
fun PlayerScreen(
    playerViewModel: PlayerViewModel,
    onSearch: () -> Unit,
    onDownloads: () -> Unit,
    onSettings: () -> Unit
) {
    val state by playerViewModel.playerState.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val progress by playerViewModel.progress.collectAsState()
    val scalingLazyListState = rememberScalingLazyListState()

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = scalingLazyListState,
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        timeText = { TimeText() }
    ) {
        item {
            Text(
                text = when (state) {
                    is PlayerViewModel.PlayerState.Playing -> (state as PlayerViewModel.PlayerState.Playing).song.title
                    is PlayerViewModel.PlayerState.Paused -> (state as PlayerViewModel.PlayerState.Paused).song.title
                    else -> "WearMusic"
                },
                style = MaterialTheme.typography.title1
            )
        }
        item {
            Text(
                text = when (state) {
                    is PlayerViewModel.PlayerState.Playing -> (state as PlayerViewModel.PlayerState.Playing).song.artist
                    is PlayerViewModel.PlayerState.Paused -> (state as PlayerViewModel.PlayerState.Paused).song.artist
                    else -> "腕上音乐"
                },
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.body1
            )
        }
        item {
            Row {
                Button(onClick = { playerViewModel.previous() }, modifier = Modifier.weight(1f)) {
                    Text("<<")
                }
                Button(onClick = { playerViewModel.togglePlayPause() }, modifier = Modifier.weight(1f)) {
                    Text(if (isPlaying) "||" else ">")
                }
                Button(onClick = { playerViewModel.next() }, modifier = Modifier.weight(1f)) {
                    Text(">>")
                }
            }
        }
        item {
            Row {
                Button(onClick = onSearch, modifier = Modifier.weight(1f)) { Text("搜索") }
                Button(onClick = onDownloads, modifier = Modifier.weight(1f)) { Text("下载") }
                Button(onClick = onSettings, modifier = Modifier.weight(1f)) { Text("设置") }
            }
        }
    }
}
