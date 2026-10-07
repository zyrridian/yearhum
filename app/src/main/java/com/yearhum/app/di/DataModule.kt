package com.yearhum.app.di

import android.content.Context
import androidx.room.Room
import com.yearhum.app.core.common.TimeProvider
import com.yearhum.app.data.local.AppDatabase
import com.yearhum.app.data.remote.musicbrainz.KtorMusicBrainzApi
import com.yearhum.app.data.remote.musicbrainz.MusicBrainzApi
import com.yearhum.app.data.repository.EnrichmentRepositoryImpl
import com.yearhum.app.data.repository.FavoritesRepositoryImpl
import com.yearhum.app.data.repository.SettingsRepositoryImpl
import com.yearhum.app.data.repository.YearRepositoryImpl
import com.yearhum.app.domain.repository.EnrichmentRepository
import com.yearhum.app.domain.repository.FavoritesRepository
import com.yearhum.app.domain.repository.SettingsRepository
import com.yearhum.app.domain.repository.YearRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase = Room
        .databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
        .createFromAsset(AppDatabase.ASSET_PATH)
        .build()

    @Provides
    fun provideCapsuleDao(db: AppDatabase) = db.capsuleDao()

    @Provides
    fun provideReleaseGroupCacheDao(db: AppDatabase) = db.releaseGroupCacheDao()

    @Provides
    fun provideFavoriteDao(db: AppDatabase) = db.favoriteDao()

    @Provides
    @Singleton
    fun provideTimeProvider(): TimeProvider = TimeProvider { System.currentTimeMillis() }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingModule {
    @Binds
    abstract fun year(impl: YearRepositoryImpl): YearRepository

    @Binds
    abstract fun enrichment(impl: EnrichmentRepositoryImpl): EnrichmentRepository

    @Binds
    abstract fun favorites(impl: FavoritesRepositoryImpl): FavoritesRepository

    @Binds
    abstract fun settings(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    abstract fun musicBrainz(impl: KtorMusicBrainzApi): MusicBrainzApi
}
