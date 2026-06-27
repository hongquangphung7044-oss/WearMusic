package com.example.wearmusic.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
            Scaffold(
