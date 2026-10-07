package com.example.yearhum.domain.model

enum class Category { SONG, ALBUM, ARTIST, GAME, MOVIE, TV, EVENT, TECH }

data class CapsuleItem(
    val id: Long,
    val year: Int,
    val category: Category,
    val rank: Int,
    val title: String,
    val subtitle: String?,
    /** MusicBrainz release-group MBID (used for Cover Art Archive lookups). */
    val mbid: String?,
    val wikidataId: String?,
    val imageUrl: String?,
    val countryCode: String?,
) {
    /** Cover Art Archive thumbnail; 404s are handled by the UI placeholder. */
    fun artworkUrl(size: Int = 250): String? =
        imageUrl ?: mbid?.let { "https://coverartarchive.org/release-group/$it/front-$size" }
}

data class YearCapsule(
    val year: Int,
    val headline: String,
    val summary: String?,
    val items: List<CapsuleItem>,
) {
    fun items(category: Category): List<CapsuleItem> = items.filter { it.category == category }
}

data class ReleaseGroupInfo(
    val mbid: String,
    val title: String,
    val artist: String,
    val firstReleaseDate: String?,
)
