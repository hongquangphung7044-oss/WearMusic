package com.example.wearmusic.data.remote

import android.util.Log
import com.example.wearmusic.data.model.Song
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.images.ArtworkFactory
import java.io.File

/**
 * 音乐标签写入器
 * 将完整的 ID3v2.4 标签写入 MP3 文件，确保其他播放器能正确识别
 */
class MusicTagWriter {

    companion object {
        private const val TAG = "MusicTagWriter"
    }

    /**
     * 歌曲标签信息
     */
    data class SongTags(
        val title: String = "",
        val artist: String = "",
        val album: String = "",
        val albumArtist: String = "",
        val year: String = "",
        val genre: String = "",
        val trackNumber: String = "",
        val comment: String = "",
        val lyrics: String = "",           // 内嵌歌词
        val coverData: ByteArray? = null,   // 封面图片二进制数据
        val coverMimeType: String = "image/jpeg"
    ) {
        fun hasBasicInfo(): Boolean {
            return title.isNotEmpty() || artist.isNotEmpty()
        }
    }

    /**
     * 从 Song 对象创建标签信息
     */
    fun createFromSong(song: Song): SongTags {
        return SongTags(
            title = song.title,
            artist = song.artist,
            album = song.album
        )
    }

    /**
     * 写入完整标签到 MP3 文件
     * @param mp3File MP3 文件路径
     * @param tags 要写入的标签信息
     * @return 是否成功
     */
    fun writeTags(mp3File: File, tags: SongTags): Boolean {
        return try {
            val audioFile = AudioFileIO.read(mp3File)
            val tag = audioFile.tagOrCreateAndSetDefault

            // 写入基本标签
            if (tags.title.isNotEmpty()) {
                tag.setField(FieldKey.TITLE, tags.title)
            }
            if (tags.artist.isNotEmpty()) {
                tag.setField(FieldKey.ARTIST, tags.artist)
            }
            if (tags.album.isNotEmpty()) {
                tag.setField(FieldKey.ALBUM, tags.album)
            }
            if (tags.albumArtist.isNotEmpty()) {
                tag.setField(FieldKey.ALBUM_ARTIST, tags.albumArtist)
            }
            if (tags.year.isNotEmpty()) {
                tag.setField(FieldKey.YEAR, tags.year)
            }
            if (tags.genre.isNotEmpty()) {
                tag.setField(FieldKey.GENRE, tags.genre)
            }
            if (tags.trackNumber.isNotEmpty()) {
                tag.setField(FieldKey.TRACK, tags.trackNumber)
            }
            if (tags.comment.isNotEmpty()) {
                tag.setField(FieldKey.COMMENT, tags.comment)
            }

            // 写入内嵌歌词
            if (tags.lyrics.isNotEmpty()) {
                tag.setField(FieldKey.LYRICS, tags.lyrics)
            }

            // 写入封面图片
            if (tags.coverData != null && tags.coverData.isNotEmpty()) {
                val artwork = ArtworkFactory.createArtworkFromBinary(
                    tags.coverData, tags.coverMimeType
                )
                tag.setField(artwork)
            }

            // 保存文件
            AudioFileIO.write(audioFile)
            Log.d(TAG, "Tags written successfully to ${mp3File.name}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write tags: ${e.message}", e)
            false
        }
    }

    /**
     * 批量写入标签到 MP3 文件（简化版）
     */
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

    /**
     * 读取现有标签（调试用）
     */
    fun readTags(mp3File: File): SongTags? {
        return try {
            val audioFile = AudioFileIO.read(mp3File)
            val tag = audioFile.tag
            SongTags(
                title = tag.getFirst(FieldKey.TITLE),
                artist = tag.getFirst(FieldKey.ARTIST),
                album = tag.getFirst(FieldKey.ALBUM),
                albumArtist = tag.getFirst(FieldKey.ALBUM_ARTIST),
                year = tag.getFirst(FieldKey.YEAR),
                genre = tag.getFirst(FieldKey.GENRE)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read tags: ${e.message}")
            null
        }
    }
}
