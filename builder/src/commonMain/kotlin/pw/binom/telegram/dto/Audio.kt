package pw.binom.telegram.dto

import kotlinx.serialization.Serializable

/**
 * This object represents an audio file to be treated as music by the Telegram clients.
 */
@Serializable
class Audio(
        /**
         * Identifier for this file, which can be used to download or reuse the file
         */
        val file_id: String,

        /**
         * Unique identifier for this file, which is supposed to be the same over time and for different bots. Can't be used to download or reuse the file.
         */
        val file_unique_id: String,

        /**
         * Duration of the audio in seconds as defined by sender
         */
        val duration: Int,

        /**
         * Performer of the audio as defined by sender or by audio tags
         */
        val performer: String?=null,

        /**
         * Title of the audio as defined by sender or by audio tags
         */
        val title: String?=null,

        /**
         * MIME type of the file as defined by sender
         */
        val mime_type: String?=null,

        /**
         * File size
         */
        val file_size: Int?=null,

        /**
         * Thumbnail of the album cover to which the music file belongs
         */
        val thumb: PhotoSize?=null
)