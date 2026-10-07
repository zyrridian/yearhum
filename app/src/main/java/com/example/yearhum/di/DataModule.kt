package com.example.yearhum.di

import android.content.Context
import androidx.room.Room
import com.example.yearhum.core.common.TimeProvider
import com.example.yearhum.data.local.AppDatabase
import com.example.yearhum.data.remote.musicbrainz.KtorMusicBrainzApi
import com.example.yearhum.data.remote.musicbrainz.MusicBrainzApi
import com.example.yearhum.data.repository.EnrichmentRepositoryImpl
import com.example.yearhum.data.repository.FavoritesRepositoryImpl
import com.example.yearhum.data.repository.SettingsRepositoryImpl
import com.example.yearhum.data.repository.YearRepositoryImpl
import com.example.yearhum.domain.repository.EnrichmentRepository
import com.example.yearhum.domain.repository.FavoritesRepository
import com.example.yearhum.domain.repository.SettingsRepository
import com.example.yearhum.domain.repository.YearRepository
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .createFromAsset(AppDatabase.ASSET_PATH)
            .build()

    @Provides fun provideCapsuleDao(db: AppDatabase) = db.capsuleDao()

    @Provides fun provideReleaseGroupCacheDao(db: AppDatabase) = db.releaseGroupCacheDao()

    @Provides fun provideFavoriteDao(db: AppDatabase) = db.favoriteDao()

    @Provides
    @Singleton
    fun provideTimeProvider(): TimeProvider = TimeProvider { System.currentTimeMillis() }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingModule {
    @Binds abstract fun year(impl: YearRepositoryImpl): YearRepository

    @Binds abstract fun enrichment(impl: EnrichmentRepositoryImpl): EnrichmentRepository

    @Binds abstract fun favorites(impl: FavoritesRepositoryImpl): FavoritesRepository

    @Binds abstract fun settings(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds abstract fun musicBrainz(impl: KtorMusicBrainzApi): MusicBrainzApi
}
