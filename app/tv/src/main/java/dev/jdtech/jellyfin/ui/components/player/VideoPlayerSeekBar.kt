package dev.jdtech.jellyfin.ui.components.player

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import dev.jdtech.jellyfin.ui.theme.FindroidTheme
import dev.jdtech.jellyfin.utils.handleBackKeyEvents
import dev.jdtech.jellyfin.utils.handleDPadKeyEvents
import kotlin.time.Duration

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoPlayerSeekBar(
    progress: Float,
    onSeek: (seekProgress: Float) -> Unit,
    state: VideoPlayerState,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester,
    contentDuration: Duration,
    seekBackIncrement: Long,
    seekForwardIncrement: Long,
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isSelected by remember { mutableStateOf(false) }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val color by rememberUpdatedState(
        newValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
    )
    val animatedHeight by animateDpAsState(
        targetValue = 8.dp.times(if (isFocused) 2f else 1f),
    )
    var seekProgress by remember { mutableFloatStateOf(0f) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isSelected) {
        if (isSelected) {
            state.showControls(seconds = Int.MAX_VALUE)
        }
    }
    LaunchedEffect(state.controlsVisible && state.quickSeek) {
        if (state.quickSeek && state.controlsVisible) {
            isSelected = true
            seekProgress = progress
        }
        if (!state.quickSeek) {
            isSelected = false
        }
    }

    var isLeftPress by remember { mutableStateOf(false) }
    var isRightPress by remember { mutableStateOf(false) }

    // make sure is a long press intent
    var leftPressCount by remember { mutableIntStateOf(0) }
    var rightPressCount by remember { mutableIntStateOf(0) }
    if (isLeftPress) {
        leftPressCount += 1
        if (leftPressCount > 3 && isSelected) {
            seekProgress =
                (seekProgress - (seekBackIncrement.toFloat() / contentDuration.inWholeMilliseconds)).coerceAtLeast(
                    0f,
                )
        }
        DisposableEffect(Unit) {
            onDispose {
                onSeek(seekProgress)
            }
        }
    } else if (isRightPress) {
        rightPressCount += 1
        if (rightPressCount > 3 && isSelected) {
            seekProgress =
                (seekProgress + (seekForwardIncrement.toFloat() / contentDuration.inWholeMilliseconds)).coerceAtMost(
                    1f,
                )
        }
        DisposableEffect(Unit) {
            onDispose {
                onSeek(seekProgress)
            }
        }
    }
    Canvas(
        modifier = modifier
            .focusRequester(focusRequester)
            .fillMaxWidth()
            .height(animatedHeight)
            .padding(horizontal = 4.dp)
            .focusable(interactionSource = interactionSource)
            .handleDPadKeyEvents(
                onRightDown = {
                    isRightPress = true
                },
                onRightUp = {
                    isRightPress = false
                    if (!isSelected) {
                        focusManager.moveFocus(FocusDirection.Right)
                    }
                },
                onLeftDown = {
                    isLeftPress = true
                },
                onLeftUp = {
                    isLeftPress = false
                    if (!isSelected) {
                        focusManager.moveFocus(FocusDirection.Left)
                    }
                },
                onEnterDown = {
                    if (isSelected) {
                        focusRequester.freeFocus()
                    } else {
                        seekProgress = progress
                    }
                    isSelected = !isSelected
                },
                onEnterUp = {
                    // ignore
                },
            )
            .handleBackKeyEvents(
                onBack = {
                    if (state.controlsVisible) {
                        state.hideControls()
                    }
                },
            ),
    ) {
        val yOffset = size.height.div(2)
        drawLine(
            color = color.copy(alpha = 0.24f),
            start = Offset(x = 0f, y = yOffset),
            end = Offset(x = size.width, y = yOffset),
            strokeWidth = size.height.div(2),
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(x = 0f, y = yOffset),
            end = Offset(
                x = size.width.times(if (isSelected) seekProgress else progress),
                y = yOffset,
            ),
            strokeWidth = size.height.div(2),
            cap = StrokeCap.Round,
        )
        drawCircle(
            color = Color.White,
            radius = size.height.div(2),
            center = Offset(
                x = size.width.times(if (isSelected) seekProgress else progress),
                y = yOffset,
            ),
        )
    }
}

@Preview
@Composable
fun VideoPlayerSeekBarPreview() {
    FindroidTheme {
        VideoPlayerSeekBar(
            progress = 0.4f,
            onSeek = {},
            state = rememberVideoPlayerState(),
            seekForwardIncrement = 10L,
            seekBackIncrement = 5L,
            focusRequester = FocusRequester(),
            contentDuration = Duration.parse("23m 40s"),
        )
    }
}
