package dev.jdtech.jellyfin.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.LibraryScreenDestination
import com.ramcosta.composedestinations.generated.destinations.MovieScreenDestination
import com.ramcosta.composedestinations.generated.destinations.ShowScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SortSelectorDialogDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultRecipient
import dev.jdtech.jellyfin.models.CollectionType
import dev.jdtech.jellyfin.models.FindroidFolder
import dev.jdtech.jellyfin.models.FindroidItem
import dev.jdtech.jellyfin.models.FindroidMovie
import dev.jdtech.jellyfin.models.FindroidShow
import dev.jdtech.jellyfin.models.SortBy
import dev.jdtech.jellyfin.ui.components.Direction
import dev.jdtech.jellyfin.ui.components.ItemCard
import dev.jdtech.jellyfin.ui.dialogs.SortItem
import dev.jdtech.jellyfin.ui.dialogs.SortSelectorDialogResult
import dev.jdtech.jellyfin.ui.dummy.dummyMovies
import dev.jdtech.jellyfin.ui.theme.FindroidTheme
import dev.jdtech.jellyfin.ui.theme.spacings
import dev.jdtech.jellyfin.viewmodels.LibraryViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.jellyfin.sdk.model.api.SortOrder
import java.util.UUID
import dev.jdtech.jellyfin.core.R as CoreR

@Destination<RootGraph>
@Composable
fun LibraryScreen(
    navigator: DestinationsNavigator,
    libraryId: UUID,
    libraryName: String,
    libraryType: CollectionType,
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    resultRecipient: ResultRecipient<SortSelectorDialogDestination, SortSelectorDialogResult>,
) {
    LaunchedEffect(true) {
        val sort = libraryViewModel.getSort()
        libraryViewModel.loadItems(libraryId, libraryType, sort.first, sort.second)
    }
    resultRecipient.onNavResult { result ->
        when (result) {
            is NavResult.Canceled -> Unit
            is NavResult.Value -> {
                val sortBy = result.value.sortBy
                val sortOrder = result.value.sortOrder
                libraryViewModel.setSort(sortBy, sortOrder)
                libraryViewModel.loadItems(libraryId, libraryType, sortBy, sortOrder)
            }
        }
    }
    val delegatedUiState by libraryViewModel.uiState.collectAsState()

    LibraryScreenLayout(
        navigator = navigator,
        libraryName = libraryName,
        uiState = delegatedUiState,
        onClick = { item ->
            when (item) {
                is FindroidMovie -> {
                    navigator.navigate(MovieScreenDestination(item.id))
                }

                is FindroidShow -> {
                    navigator.navigate(ShowScreenDestination(item.id))
                }

                is FindroidFolder -> {
                    navigator.navigate(LibraryScreenDestination(item.id, item.name, libraryType))
                }
            }
        },
    )
}

@Composable
private fun LibraryScreenLayout(
    libraryName: String,
    uiState: LibraryViewModel.UiState,
    onClick: (FindroidItem) -> Unit,
    navigator: DestinationsNavigator,
) {
    val focusRequester = remember { FocusRequester() }

    when (uiState) {
        is LibraryViewModel.UiState.Loading -> Text(text = stringResource(CoreR.string.loading))
        is LibraryViewModel.UiState.Normal -> {
            val items = uiState.items.collectAsLazyPagingItems()
            val sortItem = arrayOf(
                SortItem(
                    id = 0,
                    label = stringResource(CoreR.string.sort_name),
                    selected = uiState.sortBy == SortBy.NAME,
                    sortBy = SortBy.NAME,
                ),
                SortItem(
                    id = 1,
                    label = stringResource(CoreR.string.random),
                    selected = uiState.sortBy == SortBy.Random,
                    sortBy = SortBy.Random,
                ),
                SortItem(
                    id = 2,
                    label = stringResource(CoreR.string.community_rating),
                    selected = uiState.sortBy == SortBy.IMDB_RATING,
                    sortBy = SortBy.IMDB_RATING,
                ),
                SortItem(
                    id = 3,
                    label = stringResource(CoreR.string.critic_rating),
                    selected = uiState.sortBy == SortBy.PARENTAL_RATING,
                    sortBy = SortBy.PARENTAL_RATING,
                ),
                SortItem(
                    id = 4,
                    label = stringResource(CoreR.string.date_created),
                    selected = uiState.sortBy == SortBy.DATE_ADDED,
                    sortBy = SortBy.DATE_ADDED,
                ),
                SortItem(
                    id = 5,
                    label = stringResource(CoreR.string.date_played),
                    selected = uiState.sortBy == SortBy.DATE_PLAYED,
                    sortBy = SortBy.DATE_PLAYED,
                ),
                SortItem(
                    id = 6,
                    label = stringResource(CoreR.string.premiere_date),
                    selected = uiState.sortBy == SortBy.RELEASE_DATE,
                    sortBy = SortBy.RELEASE_DATE,
                ),
                SortItem(
                    id = 7,
                    label = stringResource(CoreR.string.series_date_played),
                    selected = uiState.sortBy == SortBy.SERIES_DATE_PLAYED,
                    sortBy = SortBy.SERIES_DATE_PLAYED,
                ),
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacings.default),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacings.default),
                contentPadding = PaddingValues(
                    horizontal = MaterialTheme.spacings.default * 2,
                    vertical = MaterialTheme.spacings.large,
                ),
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                item(span = { GridItemSpan(this.maxLineSpan) }) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacings.medium),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = libraryName,
                                style = MaterialTheme.typography.displayMedium,
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier
                                .fillMaxWidth(),
                        ) {
                            Surface(
                                modifier = Modifier.focusRequester(focusRequester),
                                onClick = {
                                    navigator.navigate(
                                        SortSelectorDialogDestination(
                                            uiState.sortOrder,
                                            sortItem,
                                        ),
                                    )
                                },
                                border = ClickableSurfaceDefaults.border(
                                    focusedBorder = Border(
                                        BorderStroke(
                                            1.dp,
                                            Color.White,
                                        ),
                                    ),
                                ),
                                colors = ClickableSurfaceDefaults.colors(
                                    containerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                ),
                                shape = ClickableSurfaceDefaults.shape(
                                    focusedShape = RoundedCornerShape(2.dp),
                                ),
                            ) {
                                Text(
                                    text = stringResource(CoreR.string.sort_by),
                                    style = MaterialTheme.typography.headlineMedium,
                                )
                            }
                        }
                    }
                }
                items(items.itemCount) { i ->
                    val item = items[i]
                    item?.let {
                        ItemCard(
                            item = item,
                            direction = Direction.VERTICAL,
                            onClick = {
                                onClick(item)
                            },
                        )
                    }
                }
            }
            LaunchedEffect(items.itemCount > 0) {
                if (items.itemCount > 0) {
                    focusRequester.requestFocus()
                }
            }
        }

        is LibraryViewModel.UiState.Error -> Text(text = uiState.error.toString())
    }
}

@Preview(device = "id:tv_1080p")
@Composable
private fun LibraryScreenLayoutPreview() {
    val data: Flow<PagingData<FindroidItem>> = flowOf(PagingData.from(dummyMovies))
    FindroidTheme {
        LibraryScreenLayout(
            libraryName = "Movies",
            uiState = LibraryViewModel.UiState.Normal(
                data,
                SortBy.defaultValue,
                SortOrder.ASCENDING,
            ),
            onClick = {},
            navigator = EmptyDestinationsNavigator,
        )
    }
}
