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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.wearmusic.player.PlayerViewModel
import com.example.wearmusic.plugin.PluginImporter
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
                Scaffold(
                    vignette = { Vignette(vignettePosition = VignettePosition.Bottom) },
                    timeText = { TimeText() }
                ) {
                    MusicPlayerScreen(playerViewModel)
                }
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
fun MusicPlayerScreen(viewModel: PlayerViewModel) {
    val state by viewModel.playerState.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.progress.collectAsState()

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
            text = currentSong?.title ?: "点击播放音乐",
            style = MaterialTheme.typography.title2,
            textAlign = TextAlign.Center
        )

        Text(
            text = currentSong?.artist ?: "WearMusic",
            style = MaterialTheme.typography.body2,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )

        CircularProgressBar(
            progress = progress,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .size(120.dp)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { viewModel.previous() },
                modifier = Modifier.size(40.dp)
            ) {
                Text("◀")
            }

            Button(
                onClick = { viewModel.togglePlayPause() },
                modifier = Modifier.size(56.dp)
            ) {
                Text(if (isPlaying) "⏸" else "▶")
            }

            Button(
                onClick = { viewModel.next() },
                modifier = Modifier.size(40.dp)
            ) {
                Text("▶")
            }
        }
    }
}

@Composable
fun CircularProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.body1
        )
    }
}
