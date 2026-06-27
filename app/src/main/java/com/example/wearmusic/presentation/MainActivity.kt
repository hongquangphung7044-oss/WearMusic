package com.example.wearmusic.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.wearmusic.data.model.Song
import com.example.wearmusic.player.PlayerViewModel
import com.example.wearmusic.plugin.PluginImporter
import com.example.wearmusic.ui.screen.DownloadItem
import com.example.wearmusic.ui.screen.DownloadsScreen
import com.example.wearmusic.ui.screen.SearchScreen
import com.example.wearmusic.ui.screen.SettingsScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val playerViewModel: PlayerViewModel by viewModels()

    @Inject
    lateinit var pluginImporter: PluginImporter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearMusicTheme {
                WearMusicNavHost(playerViewModel, pluginImporter)
            }
        }
    }
}

@Composable
fun WearMusicTheme(content: @Composable () -> Unit) {
    MaterialTheme {
        content()
    }
}

@Composable
fun WearMusicNavHost(
    viewModel: PlayerViewModel,
    pluginImporter: PluginImporter
) {
    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            Scaffold(
                vignette = { Vignette(vignettePosition = VignettePosition.Bottom) },
                timeText = { TimeText() }
            ) {
                HomeScreen(
                    playerViewModel = viewModel,
                    onSearch = { navController.navigate("search") },
                    onDownloads = { navController.navigate("downloads") },
                    onSettings = { navController.navigate("settings") }
                )
            }
        }

        composable("search") {
            Scaffold(
                vignette = { Vignette(vignettePosition = VignettePosition.Bottom) },
                timeText = { TimeText() }
            ) {
                SearchScreen(
                    onSongSelected = { song ->
                        viewModel.play(song)
                        navController.navigate("home")
                    }
                )
            }
        }

        composable("downloads") {
            Scaffold(
                vignette = { Vignette(vignettePosition = VignettePosition.Bottom) },
                timeText = { TimeText() }
            ) {
                DownloadsScreen()
            }
        }

        composable("settings") {
            Scaffold(
                vignette = { Vignette(vignettePosition = VignettePosition.Bottom) },
                timeText = { TimeText() }
            ) {
                SettingsScreen(pluginImporter = pluginImporter)
            }
        }
    }
}

@Composable
fun HomeScreen(
    playerViewModel: PlayerViewModel,
    onSearch: () -> Unit,
    onDownloads: () -> Unit,
    onSettings: () -> Unit
) {
    val state by playerViewModel.playerState.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val progress by playerViewModel.progress.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val currentSong = when (state) {
            is PlayerViewModel.PlayerState.Playing -> (state as PlayerViewModel.PlayerState.Playing).song
            is PlayerViewModel.PlayerState.Paused -> (state as PlayerViewModel.PlayerState.Paused).song
            else -> null
        }

        Text(
            text = currentSong?.title ?: "WearMusic",
            style = MaterialTheme.typography.title2,
            textAlign = TextAlign.Center
        )

        Text(
            text = currentSong?.artist ?: "",
            style = MaterialTheme.typography.body2,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )

        CircularProgressBar(
            progress = progress,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .size(100.dp)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = { playerViewModel.previous() }, modifier = Modifier.size(36.dp)) {
                Text("<<")
            }
            Button(onClick = { playerViewModel.togglePlayPause() }, modifier = Modifier.size(48.dp)) {
                Text(if (isPlaying) "||" else ">")
            }
            Button(onClick = { playerViewModel.next() }, modifier = Modifier.size(36.dp)) {
                Text(">>")
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Button(onClick = onSearch, modifier = Modifier.weight(1f)) {
                Text("搜索")
            }
            Button(onClick = onDownloads, modifier = Modifier.weight(1f)) {
                Text("下载")
            }
            Button(onClick = onSettings, modifier = Modifier.weight(1f)) {
                Text("设置")
            }
        }
    }
}

@Composable
fun CircularProgressBar(progress: Float, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.body1
        )
    }
}
