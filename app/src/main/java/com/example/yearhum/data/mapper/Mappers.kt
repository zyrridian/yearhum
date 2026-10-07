package com.example.yearhum.data.mapper

import com.example.yearhum.data.local.entity.CapsuleItemEntity
import com.example.yearhum.data.local.entity.ReleaseGroupCacheEntity
import com.example.yearhum.data.local.entity.YearCapsuleEntity
import com.example.yearhum.data.remote.musicbrainz.ReleaseGroupDto
import com.example.yearhum.domain.model.CapsuleItem
import com.example.yearhum.domain.model.Category
import com.example.yearhum.domain.model.ReleaseGroupInfo
import com.example.yearhum.domain.model.YearCapsule

fun CapsuleItemEntity.toDomain(): CapsuleItem? {
    val category = Category.entries.firstOrNull { it.name == category } ?: return null
    return CapsuleItem(
        id,
        year,
        category,
        rank,
        title,
        subtitle,
        mbid,
        wikidataId,
        imageUrl,
        countryCode
    )
}

fun YearCapsuleEntity.toDomain(items: List<CapsuleItemEntity>) =
    YearCapsule(year, headline, summary, items.mapNotNull { it.toDomain() })

fun ReleaseGroupDto.toEntity(fetchedAt: Long) = ReleaseGroupCacheEntity(
    mbid = id,
    title = title,
    artist = artistCredit.joinToString(" & ") { it.name },
    firstReleaseDate = firstReleaseDate?.takeIf { it.isNotBlank() },
    coverUrl = null,
    fetchedAt = fetchedAt,
)

fun ReleaseGroupCacheEntity.toDomain() = ReleaseGroupInfo(mbid, title, artist, firstReleaseDate)
