package dev.jdtech.jellyfin.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jdtech.jellyfin.AppPreferences
import dev.jdtech.jellyfin.models.CollectionType
import dev.jdtech.jellyfin.models.FindroidItem
import dev.jdtech.jellyfin.models.SortBy
import dev.jdtech.jellyfin.repository.JellyfinRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.SortOrder
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel
    @Inject
    constructor(
        private val jellyfinRepository: JellyfinRepository,
        private val appPreferences: AppPreferences,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
        val uiState = _uiState.asStateFlow()

        var itemsloaded = false

        sealed class UiState {
            data class Normal(
                val items: Flow<PagingData<FindroidItem>>,
                val sortBy: SortBy,
                val sortOrder: SortOrder,
                val isFilterPlayed: Boolean = false,
            ) : UiState()

            data object Loading : UiState()

            data class Error(
                val error: Exception,
            ) : UiState()
        }

        fun getSort(): Pair<SortBy, SortOrder> =
            Pair(
                SortBy.fromString(appPreferences.sortBy),
                try {
                    SortOrder.valueOf(appPreferences.sortOrder)
                } catch (_: IllegalArgumentException) {
                    SortOrder.ASCENDING
                },
            )

        fun getPlayedFilter(): Boolean = appPreferences.noPlayedFilter

        fun setPlayedFilter(noPlayedFilter: Boolean) {
            appPreferences.noPlayedFilter = noPlayedFilter
        }

        fun setSort(
            sortBy: SortBy,
            sortOrder: SortOrder,
        ) {
            appPreferences.sortBy = sortBy.sortString
            appPreferences.sortOrder = sortOrder.toString()
        }

        fun loadItems(
            parentId: UUID,
            libraryType: CollectionType,
            sortBy: SortBy = SortBy.defaultValue,
            sortOrder: SortOrder = SortOrder.ASCENDING,
            noPlayedFilter: Boolean = false,
        ) {
            itemsloaded = true
            Timber.d("$libraryType")
            val itemType =
                when (libraryType) {
                    CollectionType.Movies -> listOf(BaseItemKind.MOVIE)
                    CollectionType.TvShows -> listOf(BaseItemKind.SERIES)
                    CollectionType.BoxSets -> listOf(BaseItemKind.BOX_SET)
                    CollectionType.Mixed ->
                        listOf(
                            BaseItemKind.FOLDER,
                            BaseItemKind.MOVIE,
                            BaseItemKind.SERIES,
                        )

                    else -> null
                }

            val recursive = itemType == null || !itemType.contains(BaseItemKind.FOLDER)
            viewModelScope.launch {
                _uiState.emit(UiState.Loading)
                try {
                    val items =
                        jellyfinRepository
                            .getItemsPaging(
                                parentId = parentId,
                                includeTypes = itemType,
                                recursive = recursive,
                                // Jellyfin uses a different enum for sorting series by data played
                                sortBy =
                                    if (libraryType == CollectionType.TvShows && sortBy == SortBy.DATE_PLAYED) {
                                        SortBy.SERIES_DATE_PLAYED
                                    } else {
                                        sortBy
                                    },
                                sortOrder = sortOrder,
                                // null means no filter ，true means filter for no played
                                isPlayed =
                                    if (noPlayedFilter) {
                                        false
                                    } else {
                                        null
                                    },
                            ).cachedIn(viewModelScope)
                    _uiState.emit(UiState.Normal(items, sortBy, sortOrder, noPlayedFilter))
                } catch (e: Exception) {
                    _uiState.emit(UiState.Error(e))
                }
            }
        }
    }
