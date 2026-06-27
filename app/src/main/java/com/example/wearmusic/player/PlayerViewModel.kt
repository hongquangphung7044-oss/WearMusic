package com.example.wearmusic.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.wearmusic.data.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 播放器 ViewModel
 * 管理播放状态、队列和 UI 更新
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    application: Application,
    private val exoPlayer: ExoPlayer
) : AndroidViewModel(application) {

    sealed class PlayerState {
        data class Playing(val song: Song) : PlayerState()
        data class Paused(val song: Song) : PlayerState()
        object Idle : PlayerState()
        data class Error(val message: String) : PlayerState()
    }

    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.Idle)
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_READY -> _isPlaying.value = exoPlayer.isPlaying
                    Player.STATE_ENDED -> next()
                    Player.STATE_IDLE -> _isPlaying.value = false
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }
        })

        // 进度轮询
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                if (exoPlayer.duration > 0) {
                    _progress.value = exoPlayer.currentPosition.toFloat() / exoPlayer.duration
                }
            }
        }
    }

    fun play(song: Song, queue: List<Song> = listOf(song)) {
        _queue.value = queue
        _currentIndex.value = queue.indexOfFirst { it.id == song.id }

        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id)
            .setUri(song.mediaUrl.takeIf { it.isNotEmpty() } ?: song.localPath)
            .build()

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
        _playerState.value = PlayerState.Playing(song)
    }

    fun pause() {
        exoPlayer.pause()
        val current = _queue.value.getOrNull(_currentIndex.value)
        if (current != null) {
            _playerState.value = PlayerState.Paused(current)
        }
    }

    fun resume() {
        exoPlayer.play()
    }

    fun next() {
        val nextIndex = (_currentIndex.value + 1).coerceAtMost(_queue.value.size - 1)
        if (nextIndex != _currentIndex.value) {
            _currentIndex.value = nextIndex
            _queue.value.getOrNull(nextIndex)?.let { play(it, _queue.value) }
        }
    }

    fun previous() {
        val prevIndex = (_currentIndex.value - 1).coerceAtLeast(0)
        if (prevIndex != _currentIndex.value) {
            _currentIndex.value = prevIndex
            _queue.value.getOrNull(prevIndex)?.let { play(it, _queue.value) }
        }
    }

    fun seekTo(progress: Float) {
        val duration = exoPlayer.duration
        if (duration > 0) {
            exoPlayer.seekTo((progress * duration).toLong())
        }
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            pause()
        } else {
            resume()
        }
    }

    override fun onCleared() {
        super.onCleared()
        // 注：ExoPlayer 由 Service 生命周期管理，这里不释放
    }
}
