package pw.binom.builder.web.dto

import kotlin.js.Json

/**
 * Contains last lines of output logs and number of bytes before [text]
 * @param text last line of logs
 * @param skipped count of bytes before [text]
 */
class LastOutDto(val text: String, val skipped: Long) {
    companion object {
        fun read(node: Json) = LastOutDto(
                text = node["text"]!!.toString(),
                skipped = node["skipped"].toString().toLong()
        )
    }
}