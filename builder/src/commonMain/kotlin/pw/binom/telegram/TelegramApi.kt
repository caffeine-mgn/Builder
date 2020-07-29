package pw.binom.telegram

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.*
import pw.binom.URL
import pw.binom.io.*
import pw.binom.io.http.Headers
import pw.binom.io.httpClient.AsyncHttpClient
import pw.binom.io.socket.nio.SocketNIOManager
import pw.binom.telegram.dto.Markup
import pw.binom.telegram.dto.Message
import pw.binom.telegram.dto.Update

class TelegramApi(var lastUpdate: Long, val token: String, manager: SocketNIOManager) {
    val client = AsyncHttpClient(manager)
    val baseUrl = URL("https://api.telegram.org/bot${UTF8.urlEncode(token)}")

    private val jj = Json(JsonConfiguration.Default.copy(
            ignoreUnknownKeys = true,
            isLenient = true,
            serializeSpecialFloatingPointValues = true,
            allowStructuredMapKeys = true,
            encodeDefaults = false
    ))

    @Serializable
    enum class ParseMode {
        @SerialName("MarkdownV2")
        MARKDOWN_V2,

        @SerialName("HTML")
        HTML,

        @SerialName("Markdown")
        MARKDOWN
    }

    @Serializable
    class TextMessage(
            /**
             * Unique identifier for the target chat or username of the target channel (in the format @channelusername)
             */
            @SerialName("chat_id")
            val chat_id: String,

            /**
             * Text of the message to be sent, 1-4096 characters after entities parsing
             */
            @SerialName("text")
            val text: String,

            /**
             * Mode for parsing entities in the message text. See [ParseMode] for more details.
             */
            @SerialName("parse_mode")
            val parseMode: ParseMode? = null,

            /**
             * Disables link previews for links in this message
             */
            @SerialName("disable_web_page_preview")
            val disableWebPagePreview: Boolean? = null,

            /**
             * Sends the message silently. Users will receive a notification with no sound.
             */
            @SerialName("disable_notification")
            val disableNotification: Boolean? = null,

            /**
             * If the message is a reply, ID of the original message
             */
            @SerialName("reply_to_message_id")
            val replyToMessageId: Long? = null,

            /**
             * Additional interface options. A JSON-serialized object for an inline keyboard, custom reply keyboard, instructions to remove reply keyboard or to force a reply from the user.
             */
            @SerialName("reply_markup")
            val replyMarkup: Markup? = null
    )

    suspend fun sendMessage(message: TextMessage): Message {
        var url = baseUrl.appendDirectionURI("sendMessage")
        val response = client.request("POST", url)
                .addHeader(Headers.CONTENT_TYPE, "application/json")
                .upload().also {
                    it.utf8Appendable().append(jj.stringify(TextMessage.serializer(), message))
                    Unit
                }.response()
        val responseText = response.utf8Reader().use { it.readText() }
        return jj.fromJson(Message.serializer(), getResult(responseText)!!)
    }

    @OptIn(UnstableDefault::class)
    suspend fun getUpdate(): List<Update> {
        var url = baseUrl.appendDirectionURI("getUpdates?offset=${lastUpdate + 1}&timeout=${60}")
        val json = client.request("GET", url)
                .response()
                .utf8Reader().use {
                    it.readText()
                }
        val resultJsonTree = getResult(json)
        val updates = jj.fromJson(Update.serializer().list, resultJsonTree!!.jsonArray)
        val updateId = updates.lastOrNull()?.updateId
        if (updateId != null) {
            lastUpdate = updateId
        }
        return updates
    }

    private fun getResult(json: String): JsonElement? {
        val tree = Json.parseJson(json).jsonObject
        if (tree["ok"]?.boolean != true) {
            val code = tree["error_code"]!!.int
            throw when (code) {
                400 -> ChatNotFoundException()
                else -> TelegramException(
                        code = code,
                        description = tree["description"]!!.content
                )
            }
        }
        return tree["result"]
    }

    abstract class AbstractTelegramException : IOException {
        constructor() : super()
        constructor(message: String?) : super(message)
        constructor(message: String?, cause: Throwable?) : super(message, cause)
        constructor(cause: Throwable?) : super(cause)
    }

    class ChatNotFoundException : AbstractTelegramException()

    class TelegramException(val code: Int, val description: String) : AbstractTelegramException() {
        override val message: String?
            get() = "$code: $description"
    }
}