package builder

import kotlinx.serialization.Serializable

/** A row scraped from a Wikipedia list, before MusicBrainz resolution. */
data class RawEntry(val year: Int, val category: String, val rank: Int, val title: String, val artist: String)

@Serializable
data class BuiltItem(
    val year: Int,
    val category: String,
    val rank: Int,
    val title: String,
    val subtitle: String?,
    val mbid: String?,
)

@Serializable
data class BuiltCapsule(val year: Int, val headline: String, val items: List<BuiltItem>)

@Serializable
data class Snapshot(val datasetVersion: Int, val capsules: List<BuiltCapsule>)
