package com.example.wearmusic.di

import android.content.Context
import android.os.Looper
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import com.example.wearmusic.data.remote.CoverService
import com.example.wearmusic.data.remote.LyricsService
import com.example.wearmusic.data.remote.MusicSearchService
import com.example.wearmusic.data.remote.MusicTagWriter
import com.example.wearmusic.plugin.PluginImporter
import com.example.wearmusic.plugin.PluginRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .setLooper(Looper.getMainLooper())
            .build()
    }

    @Provides
    @Singleton
    fun provideLyricsService(client: OkHttpClient): LyricsService {
        return LyricsService(client)
    }

    @Provides
    @Singleton
    fun provideCoverService(client: OkHttpClient): CoverService {
        return CoverService(client)
    }

    @Provides
    @Singleton
    fun provideMusicSearchService(client: OkHttpClient): MusicSearchService {
        return MusicSearchService(client)
    }

    @Provides
    @Singleton
    fun provideMusicTagWriter(): MusicTagWriter {
        return MusicTagWriter()
    }

    @Provides
    @Singleton
    fun providePluginImporter(
        @ApplicationContext context: Context,
        client: OkHttpClient
    ): PluginImporter {
        return PluginImporter(context, client)
    }

    @Provides
    @Singleton
    fun providePluginRepository(
        @ApplicationContext context: Context,
        pluginImporter: PluginImporter
    ): PluginRepository {
        return PluginRepository(context, pluginImporter)
    }
}
