package pw.binom.telegram.dto

import kotlinx.serialization.Serializable

/**
 * This object represents an animation file (GIF or H.264/MPEG-4 AVC video without sound).
 */
@Serializable
class Animation(
        /**
         * Identifier for this file, which can be used to download or reuse the file
         */
        val file_id: String,

        /**
         * Unique identifier for this file, which is supposed to be the same over time and for different bots. Can't be used to download or reuse the file.
         */
        val file_unique_id: String,

        /**
         * Video width as defined by sender
         */
        val width: Int,

        /**
         * Video height as defined by sender
         */
        val height: Int,

        /**
         * Duration of the video in seconds as defined by sender
         */
        val duration: Int,

        /**
         * Animation thumbnail as defined by sender
         */
        val thumb: PhotoSize?=null,

        /**
         * Original animation filename as defined by sender
         */
        val file_name: String?=null,

        /**
         * MIME type of the file as defined by sender
         */
        val mime_type: String?=null,

        /**
         * File size
         */
        val file_size: Int?=null
)