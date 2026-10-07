package com.yearhum.app.data.mapper

import com.yearhum.app.data.local.entity.CapsuleItemEntity
import com.yearhum.app.data.local.entity.ReleaseGroupCacheEntity
import com.yearhum.app.data.local.entity.YearCapsuleEntity
import com.yearhum.app.data.remote.musicbrainz.ReleaseGroupDto
import com.yearhum.app.domain.model.CapsuleItem
import com.yearhum.app.domain.model.Category
import com.yearhum.app.domain.model.ReleaseGroupInfo
import com.yearhum.app.domain.model.YearCapsule

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
        countryCode,
    )
}

fun YearCapsuleEntity.toDomain(items: List<CapsuleItemEntity>) = YearCapsule(year, headline, summary, items.mapNotNull { it.toDomain() })

fun ReleaseGroupDto.toEntity(fetchedAt: Long) =
    ReleaseGroupCacheEntity(
        mbid = id,
        title = title,
        artist = artistCredit.joinToString(" & ") { it.name },
        firstReleaseDate = firstReleaseDate?.takeIf { it.isNotBlank() },
        coverUrl = null,
        fetchedAt = fetchedAt,
    )

fun ReleaseGroupCacheEntity.toDomain() = ReleaseGroupInfo(mbid, title, artist, firstReleaseDate)
