package com.example.yearhum.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "year_capsule")
data class YearCapsuleEntity(
    @PrimaryKey val year: Int,
    val headline: String,
    val summary: String?,
)

@Entity(
    tableName = "capsule_item",
    foreignKeys = [
        ForeignKey(
            entity = YearCapsuleEntity::class,
            parentColumns = ["year"],
            childColumns = ["year"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["year", "category", "rank"]), Index(value = ["mbid"])],
)
data class CapsuleItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val year: Int,
    /** Name of [com.example.yearhum.domain.model.Category]. */
    val category: String,
    val rank: Int,
    val title: String,
    val subtitle: String?,
    val mbid: String?,
    val wikidataId: String?,
    val imageUrl: String?,
    val countryCode: String?,
)

@Entity(tableName = "release_group_cache")
data class ReleaseGroupCacheEntity(
    @PrimaryKey val mbid: String,
    val title: String,
    val artist: String,
    val firstReleaseDate: String?,
    @ColumnInfo(name = "coverUrl") val coverUrl: String?,
    val fetchedAt: Long,
)

@Entity(tableName = "favorite")
data class FavoriteEntity(
    @PrimaryKey val itemId: Long,
    val addedAt: Long,
)
