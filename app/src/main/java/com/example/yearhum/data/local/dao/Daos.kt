package com.example.yearhum.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.yearhum.data.local.entity.CapsuleItemEntity
import com.example.yearhum.data.local.entity.FavoriteEntity
import com.example.yearhum.data.local.entity.ReleaseGroupCacheEntity
import com.example.yearhum.data.local.entity.YearCapsuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CapsuleDao {
    @Query("SELECT year FROM year_capsule ORDER BY year")
    fun observeYears(): Flow<List<Int>>

    @Query("SELECT * FROM year_capsule WHERE year = :year")
    fun observeCapsule(year: Int): Flow<YearCapsuleEntity?>

    @Query("SELECT * FROM capsule_item WHERE year = :year ORDER BY category, rank")
    fun observeItems(year: Int): Flow<List<CapsuleItemEntity>>

    @Query("SELECT * FROM capsule_item WHERE id = :id")
    fun observeItem(id: Long): Flow<CapsuleItemEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCapsules(capsules: List<YearCapsuleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<CapsuleItemEntity>)
}

@Dao
interface ReleaseGroupCacheDao {
    @Query("SELECT * FROM release_group_cache WHERE mbid = :mbid")
    suspend fun get(mbid: String): ReleaseGroupCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ReleaseGroupCacheEntity)

    @Query("DELETE FROM release_group_cache")
    suspend fun clear()
}

@Dao
interface FavoriteDao {
    @Query("SELECT EXISTS(SELECT 1 FROM favorite WHERE itemId = :itemId)")
    fun observeIsFavorite(itemId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(favorite: FavoriteEntity): Long

    @Query("DELETE FROM favorite WHERE itemId = :itemId")
    suspend fun remove(itemId: Long)

    @Query(
        "SELECT capsule_item.* FROM capsule_item " +
            "INNER JOIN favorite ON favorite.itemId = capsule_item.id ORDER BY favorite.addedAt DESC",
    )
    fun observeFavoriteItems(): Flow<List<CapsuleItemEntity>>
}
