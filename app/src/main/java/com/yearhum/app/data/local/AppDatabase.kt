package com.yearhum.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yearhum.app.data.local.dao.CapsuleDao
import com.yearhum.app.data.local.dao.FavoriteDao
import com.yearhum.app.data.local.dao.ReleaseGroupCacheDao
import com.yearhum.app.data.local.entity.CapsuleItemEntity
import com.yearhum.app.data.local.entity.FavoriteEntity
import com.yearhum.app.data.local.entity.ReleaseGroupCacheEntity
import com.yearhum.app.data.local.entity.YearCapsuleEntity

/**
 * Version is bumped together with the generated asset DB (`databases/capsules.db`),
 * see tools/dataset-builder.
 */
@Database(
    entities = [
        YearCapsuleEntity::class,
        CapsuleItemEntity::class,
        ReleaseGroupCacheEntity::class,
        FavoriteEntity::class,
    ],
    version = AppDatabase.VERSION,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun capsuleDao(): CapsuleDao

    abstract fun releaseGroupCacheDao(): ReleaseGroupCacheDao

    abstract fun favoriteDao(): FavoriteDao

    companion object {
        const val VERSION = 1
        const val ASSET_PATH = "databases/capsules.db"
        const val NAME = "yearhum.db"
    }
}
