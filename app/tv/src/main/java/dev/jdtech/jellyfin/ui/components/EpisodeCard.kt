package dev.jdtech.jellyfin.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import dev.jdtech.jellyfin.models.FindroidEpisode
import dev.jdtech.jellyfin.ui.dummy.dummyEpisode
import dev.jdtech.jellyfin.ui.theme.FindroidTheme
import dev.jdtech.jellyfin.ui.theme.spacings

@Composable
fun EpisodeCard(
    episode: FindroidEpisode,
    index: Int,
    currentIndex: Int,
    onPlayClick: (FindroidEpisode) -> Unit,
    onReplayClick: (FindroidEpisode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .border(
                border = if ((index - 1) == currentIndex) {
                    BorderStroke(
                        4.dp,
                        Color.White,
                    )
                } else {
                    BorderStroke(
                        0.dp,
                        Color.Transparent,
                    )
                },
                shape = if ((index - 1) == currentIndex) {
                    RoundedCornerShape(10.dp)
                } else {
                    RoundedCornerShape(
                        0.dp,
                    )
                },
            )
            .background(color = Color.Transparent)
            .fillMaxWidth(),
    ) {
        Row(
            modifier = modifier.padding(MaterialTheme.spacings.small),
        ) {
            Box(modifier = modifier.width(160.dp)) {
                ItemPoster(
                    item = episode,
                    direction = Direction.HORIZONTAL,
                    modifier = modifier.clip(RoundedCornerShape(10.dp)),
                )
                PlayControlHover(
                    item = episode,
                    onPlayClick = { onPlayClick(episode) },
                    onReplayClick = { onReplayClick(episode) },
                    modifier = modifier
                        .align(Alignment.Center),
                )
                ProgressBadge(
                    item = episode,
                    modifier = modifier
                        .align(Alignment.TopEnd)
                        .padding(PaddingValues(MaterialTheme.spacings.small)),
                )
            }
            Spacer(modifier = modifier.width(MaterialTheme.spacings.medium))
            Column {
                Text(
                    text = stringResource(
                        id = dev.jdtech.jellyfin.core.R.string.episode_name,
                        episode.indexNumber,
                        episode.name,
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacings.extraSmall))
                Text(
                    text = episode.overview,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview
@Composable
private fun ItemCardPreviewEpisode() {
    FindroidTheme {
        EpisodeCard(
            episode = dummyEpisode,
            onPlayClick = {},
            onReplayClick = {},
            index = 2,
            currentIndex = 1,
        )
    }
}
