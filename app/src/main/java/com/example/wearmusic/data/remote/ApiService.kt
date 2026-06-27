package com.example.wearmusic.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit API 接口
 * 插件会提供具体的 baseUrl
 */
interface MusicApiService {
    @GET("search")
    suspend fun search(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): SearchResponse

    @GET("song/detail")
    suspend fun getSongDetail(@Query("id") id: String): SongDetailResponse

    @GET("lyrics")
    suspend fun getLyrics(@Query("id") id: String): LyricsResponse
}

data class SearchResponse(val songs: List<SongDto> = emptyList())
data class SongDetailResponse(val url: String = "", val bitrate: Int = 320)
data class LyricsResponse(val lyrics: String = "")

data class SongDto(
    val id: String,
    val title: String,
    val artist: String,
    val album: String?,
    val duration: Long?
)
