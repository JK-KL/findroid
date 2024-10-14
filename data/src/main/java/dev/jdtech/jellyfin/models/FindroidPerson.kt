package dev.jdtech.jellyfin.models

import android.net.Uri
import org.jellyfin.sdk.model.api.BaseItemPerson
import org.jellyfin.sdk.model.api.ImageType
import org.jellyfin.sdk.model.api.PersonKind
import java.util.UUID

data class FindroidPerson(
    override val id: UUID,
    override val name: String,
    val role: String,
    val type: PersonKind,
    override val originalTitle: String? = null,
    override val overview: String = "",
    override val played: Boolean = false,
    override val favorite: Boolean = false,
    override val canPlay: Boolean = false,
    override val canDownload: Boolean = false,
    override val sources: List<FindroidSource> = emptyList(),
    override val runtimeTicks: Long = 0L,
    override val playbackPositionTicks: Long = 0L,
    override val unplayedItemCount: Int? = null,
    override val images: FindroidImages,
    override val chapters: List<FindroidChapter>? = null,
) : FindroidItem

fun BaseItemPerson.toFindroidPerson(baseUri: String): FindroidPerson {
    return FindroidPerson(
        id = this.id,
        name = this.name.orEmpty(),
        role = this.role.orEmpty(),
        type = this.type,
        images = toFindroidImages(this.id.toString(), baseUri, this.primaryImageTag, this.imageBlurHashes),
    )
}

fun toFindroidImages(
    id: String,
    baseUri: String,
    imageTags: String?,
    imageBlurHashes: Map<ImageType, Map<String, String>>?,
): FindroidImages {
    val baseUrl = Uri.parse(baseUri)
    val primary = imageBlurHashes?.get(ImageType.PRIMARY)?.let { tag ->
        baseUrl.buildUpon()
            .appendEncodedPath("items/$id/Images/${ImageType.PRIMARY}")
            .appendQueryParameter("tag", imageTags)
            .build()
    }
    return FindroidImages(
        primary = primary,
    )
}
