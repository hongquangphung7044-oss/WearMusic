package com.example.wearmusic.data.remote

import android.util.Log
import com.example.wearmusic.data.model.Song
import com.mpatric.mp3agic.Mp3File
import java.io.File

/**
 * 音乐标签写入器
 * 使用 mp3agic 库（兼容 Android），将完整的 ID3v2 标签写入 MP3 文件
 */
class MusicTagWriter {

    companion object {
        private const val TAG = "MusicTagWriter"
    }

    data class SongTags(
        val title: String = "",
        val artist: String = "",
        val album: String = "",
        val albumArtist: String = "",
        val year: String = "",
        val genre: String = "",
        val trackNumber: String = "",
        val comment: String = "",
        val lyrics: String = "",
        val coverData: ByteArray? = null,
        val coverMimeType: String = "image/jpeg"
    ) {
        fun hasBasicInfo(): Boolean = title.isNotEmpty() || artist.isNotEmpty()
    }

    fun createFromSong(song: Song): SongTags {
        return SongTags(
            title = song.title,
            artist = song.artist,
            album = song.album
        )
    }

    fun writeTags(mp3File: File, tags: SongTags): Boolean {
        return try {
            val mp3 = Mp3File(mp3File.absolutePath)
            var tag = mp3.id3v2Tag
            if (tag == null) {
                mp3.removeId3v1Tag()
                mp3.removeCustomTag()
                tag = com.mpatric.mp3agic.ID3v24Tag()
                mp3.id3v2Tag = tag
            }

            if (tags.title.isNotEmpty()) tag.title = tags.title
            if (tags.artist.isNotEmpty()) tag.artist = tags.artist
            if (tags.album.isNotEmpty()) tag.album = tags.album
            if (tags.albumArtist.isNotEmpty()) tag.albumArtist = tags.albumArtist
            if (tags.year.isNotEmpty()) tag.year = tags.year
            if (tags.genre.isNotEmpty()) tag.genreDescription = tags.genre
            if (tags.trackNumber.isNotEmpty()) tag.track = tags.trackNumber
            if (tags.comment.isNotEmpty()) tag.comment = tags.comment
            if (tags.lyrics.isNotEmpty()) tag.lyrics = tags.lyrics

            if (tags.coverData != null && tags.coverData.isNotEmpty()) {
                tag.setAlbumImage(tags.coverData, tags.coverMimeType)
            }

            mp3.save(mp3File.absolutePath)
            Log.d(TAG, "Tags written successfully to ${mp3File.name}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write tags: ${e.message}", e)
            false
        }
    }

    fun writeSongTags(
        mp3File: File,
        song: Song,
        lyrics: String = "",
        coverFile: File? = null
    ): Boolean {
        val tags = createFromSong(song).copy(
            lyrics = lyrics,
            coverData = coverFile?.readBytes(),
            coverMimeType = when (coverFile?.extension?.lowercase()) {
                "png" -> "image/png"
                "gif" -> "image/gif"
                else -> "image/jpeg"
            }
        )
        return writeTags(mp3File, tags)
    }

    fun readTags(mp3File: File): SongTags? {
        return try {
            val mp3 = Mp3File(mp3File.absolutePath)
            val tag = mp3.id3v2Tag ?: return null
            SongTags(
                title = tag.title ?: "",
                artist = tag.artist ?: "",
                album = tag.album ?: "",
                albumArtist = tag.albumArtist ?: "",
                year = tag.year ?: "",
                genre = tag.genreDescription ?: ""
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read tags: ${e.message}")
            null
        }
    }
}
