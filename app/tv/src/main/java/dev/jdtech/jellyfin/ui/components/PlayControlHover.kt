package dev.jdtech.jellyfin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import dev.jdtech.jellyfin.models.FindroidEpisode
import dev.jdtech.jellyfin.ui.dummy.dummyEpisode
import dev.jdtech.jellyfin.ui.theme.FindroidTheme
import dev.jdtech.jellyfin.ui.theme.spacings
import dev.jdtech.jellyfin.core.R as CoreR

@Composable
fun PlayControlHover(
    item: FindroidEpisode,
    modifier: Modifier = Modifier,
    onPlayClick: (FindroidEpisode) -> Unit,
    onReplayClick: (FindroidEpisode) -> Unit,
) {
    Box(
        modifier = modifier
            .background(Color.Transparent),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacings.medium),
        ) {
            Column {
                IconButton(
                    modifier = modifier,
                    onClick = {
                        onPlayClick(item)
                    },
                ) {
                    Icon(
                        painter = painterResource(id = CoreR.drawable.ic_play),
                        contentDescription = "",
                    )
                }
            }
            Column {
                IconButton(
                    modifier = modifier,
                    onClick = {
                        onReplayClick(item)
                    },
                ) {
                    Icon(
                        painter = painterResource(id = CoreR.drawable.ic_rotate_ccw),
                        contentDescription = "",
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ProgressBadgePreviewWatched() {
    FindroidTheme {
        PlayControlHover(
            item = dummyEpisode,
            onReplayClick = {},
            onPlayClick = {},
        )
    }
}
