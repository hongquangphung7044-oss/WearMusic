package com.example.wearmusic.download

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.wearmusic.data.model.Song
import com.example.wearmusic.data.remote.CoverService
import com.example.wearmusic.data.remote.LyricsService
import com.example.wearmusic.data.remote.MusicTagWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

/**
 * 下载 Worker：下载歌曲 + 封面 + 歌词 + 写入 ID3 标签
 * 使用 WorkManager 在后台执行
 */
class DownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val client = OkHttpClient()
    private val lyricsService = LyricsService(client)
    private val coverService = CoverService(client)
    private val tagWriter = MusicTagWriter()

    companion object {
        const val KEY_SONG_ID = "song_id"
        const val KEY_SONG_TITLE = "song_title"
        const val KEY_SONG_ARTIST = "song_artist"
        const val KEY_SONG_ALBUM = "song_album"
        const val KEY_MEDIA_URL = "media_url"
        const val KEY_COVER_URL = "cover_url"
    }

    override suspend fun doWork(): Result {
        val songId = inputData.getString(KEY_SONG_ID) ?: return Result.failure()
        val title = inputData.getString(KEY_SONG_TITLE) ?: "unknown"
        val artist = inputData.getString(KEY_SONG_ARTIST) ?: "unknown"
        val album = inputData.getString(KEY_SONG_ALBUM) ?: ""
        val mediaUrl = inputData.getString(KEY_MEDIA_URL) ?: return Result.failure()
        val coverUrl = inputData.getString(KEY_COVER_URL) ?: ""

        return withContext(Dispatchers.IO) {
            try {
                val baseDir = applicationContext.getExternalFilesDir("music")
                    ?: applicationContext.filesDir
                val songDir = File(baseDir, songId)
                songDir.mkdirs()

                val mp3File = File(songDir, "$title.mp3")

                // 1. 下载歌曲
                downloadFile(mediaUrl, mp3File)

                // 2. 搜索并下载封面
                var finalCoverFile: File? = null
                val coverFile = File(songDir, "cover.jpg")
                if (coverUrl.isEmpty()) {
                    // 网络搜索封面
                    val searchedCover = coverService.searchCover(title, artist)
                    if (searchedCover.isNotEmpty()) {
                        coverService.downloadCover(searchedCover, coverFile)
                        if (coverFile.exists()) {
                            finalCoverFile = coverFile
                        }
                    }
                } else {
                    // 使用提供的 URL 下载
                    downloadFile(coverUrl, coverFile)
                    if (coverFile.exists()) {
                        finalCoverFile = coverFile
                    }
                }

                // 3. 搜索歌词
                val lyrics = lyricsService.searchLyrics(title, artist)
                val hasLyrics = lyrics.isNotEmpty() && !lyrics.contains("失败")

                // 4. 写入 ID3 标签
                val song = Song(id = songId, title = title, artist = artist, album = album)
                val tags = MusicTagWriter.SongTags(
                    title = title,
                    artist = artist,
                    album = album,
                    lyrics = if (hasLyrics) lyrics else "",
                    coverData = finalCoverFile?.readBytes(),
                    coverMimeType = when (finalCoverFile?.extension?.lowercase()) {
                        "png" -> "image/png"
                        "gif" -> "image/gif"
                        else -> "image/jpeg"
                    }
                )
                tagWriter.writeTags(mp3File, tags)

                // 5. 保存元数据（备用，用于应用内读取）
                val metadataFile = File(songDir, "metadata.json")
                metadataFile.writeText(
                    """{"id":"$songId","title":"$title","artist":"$artist","album":"$album","song":"${mp3File.name}","cover":"${finalCoverFile?.name ?: ""}","lyrics":"${if (hasLyrics) "lyrics.lrc" else ""}"}"""
                )

                // 6. 可选：单独保存歌词文件（供歌词显示使用）
                if (hasLyrics) {
                    File(songDir, "lyrics.lrc").writeText(lyrics)
                }

                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.retry()
            }
        }
    }

    private fun downloadFile(url: String, targetFile: File) {
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful || response.body == null) {
            throw Exception("Download failed for $url")
        }
        response.body!!.byteStream().use { input ->
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
            }
        }
    }
}
