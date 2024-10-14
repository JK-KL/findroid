package dev.jdtech.jellyfin.ui.components.player

import androidx.annotation.IntRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce

class VideoPlayerState internal constructor(
    @IntRange(from = 0)
    private val hideSeconds: Int,
) {
    private var _controlsVisible by mutableStateOf(true)
    val controlsVisible get() = _controlsVisible

    private var _quickSeek by mutableStateOf(false)
    val quickSeek get() = _quickSeek

    private var _seeking by mutableStateOf(false)
    val isSeeking get() = _seeking

    fun showControls(seconds: Int = hideSeconds) {
        _controlsVisible = true
        channel.trySend(seconds)
    }
    fun hideControls() {
        _controlsVisible = false
        _quickSeek = false
    }

    fun quickSeekMode() {
        _controlsVisible = true
        _quickSeek = true
        channel.trySend(Int.MAX_VALUE)
    }
    fun quitQuickSeekMode() {
        _quickSeek = false
        channel.trySend(2)
    }


    private val channel = Channel<Int>(CONFLATED)

    private val seekChannel = Channel<Int>(CONFLATED)

    @OptIn(FlowPreview::class)
    suspend fun observe() {
        channel.consumeAsFlow()
            .debounce { it.toLong() * 1000 }
            .collect {
                _controlsVisible = false
                _quickSeek = false
            }
    }

    @OptIn(FlowPreview::class)
    suspend fun effectSeek() {
        seekChannel.consumeAsFlow()
            .debounce { it.toLong() * 1000 }
            .collect {
                _seeking = false
            }
    }
}

@Composable
fun rememberVideoPlayerState(
    @IntRange(from = 0) hideSeconds: Int = 2,
) =
    remember {
        VideoPlayerState(hideSeconds = hideSeconds)
    }
        .also {
            LaunchedEffect(it) { it.observe() }
        }.also {
            LaunchedEffect(it) { it.effectSeek() }
        }
