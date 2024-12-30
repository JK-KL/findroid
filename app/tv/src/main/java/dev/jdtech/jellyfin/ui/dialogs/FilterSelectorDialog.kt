package dev.jdtech.jellyfin.ui.dialogs

import android.os.Parcelable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Checkbox
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ClickableSurfaceScale
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.result.EmptyResultBackNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator
import dev.jdtech.jellyfin.ui.theme.FindroidTheme
import dev.jdtech.jellyfin.ui.theme.spacings
import dev.jdtech.jellyfin.utils.handleBackKeyEvents
import kotlinx.parcelize.Parcelize
import dev.jdtech.jellyfin.core.R as CoreR

@Parcelize
data class FilterSelectorDialogResult(
    val id: List<Int>,
) : Parcelable

@Parcelize
data class FilterItem(
    val id: Int,
    val selected: Boolean,
    val label: String,
    val enabled: Boolean = true,
) : Parcelable

@Destination<RootGraph>(style = BaseDialogStyle::class)
@Composable
fun FilterSelectorDialog(
    sorts: Array<FilterItem>,
    resultNavigator: ResultBackNavigator<FilterSelectorDialogResult>,
) {
    val sortSelected = remember {
        mutableStateListOf<Boolean>().apply {
            for (i in sorts) {
                if (i.selected) {
                    add(true)
                } else {
                    add(false)
                }
            }
        }
    }
    Surface {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacings.medium),
        ) {
            Text(
                text = stringResource(id = CoreR.string.filter_by),
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacings.medium))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacings.medium - MaterialTheme.spacings.extraSmall),
                contentPadding = PaddingValues(vertical = MaterialTheme.spacings.extraSmall),
            ) {
                itemsIndexed(sorts) { index, sort ->
                    Surface(
                        onClick = {
                            sortSelected[index] = !sortSelected[index]
                        },
                        enabled = true,
                        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(4.dp)),
                        colors =
                            ClickableSurfaceDefaults.colors(
                                containerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                            ),
                        border =
                            ClickableSurfaceDefaults.border(
                                focusedBorder =
                                    Border(
                                        BorderStroke(
                                            4.dp,
                                            Color.White,
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                    ),
                            ),
                        scale = ClickableSurfaceScale.None,
                        modifier = Modifier.handleBackKeyEvents(
                            onBack = {
                                resultNavigator.navigateBack(
                                    result = FilterSelectorDialogResult(
                                        sortSelected.withIndex()
                                            .filter { a -> a.value }
                                            .map { a -> a.index },
                                    ),
                                )
                            },
                        ),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(MaterialTheme.spacings.extraSmall),
                        ) {
                            Checkbox(
                                checked = sortSelected[index],
                                onCheckedChange = null,
                            )
                            Spacer(modifier = Modifier.width(MaterialTheme.spacings.extraSmall))
                            Text(
                                text = sort.label,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(end = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun FilterSelectorDialogPreview() {
    FindroidTheme {
        FilterSelectorDialog(
            sorts =
                arrayOf(
                    FilterItem(
                        id = 0,
                        label = "name",
                        selected = true,
                    ),
                    FilterItem(
                        id = 1,
                        label = "date",
                        selected = false,
                    ),
                ),
            resultNavigator = EmptyResultBackNavigator(),
        )
    }
}
