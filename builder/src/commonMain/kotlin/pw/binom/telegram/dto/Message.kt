package pw.binom.telegram.dto

import kotlinx.serialization.Serializable

@Serializable
class Message(
        val message_id: Long,
        val from: User? = null,
        val date: Long,
        val chat: Chat,
        val forward_from: User? = null,
        val forward_from_chat: Chat? = null,
        val forward_from_message_id: Long? = null,
        val forward_signature: String? = null,
        val forward_sender_name: String? = null,
        val forward_date: Long? = null,
        val reply_to_message: Message? = null,
        val via_bot: User? = null,
        val edit_date: Long? = null,
        val media_group_id: String? = null,
        val author_signature: String? = null,
        val text: String? = null,
        val entities: List<MessageEntity>? = null,
        val animation: Animation? = null,
        val audio: Audio? = null,
        val document: Document? = null,
        val photo: List<PhotoSize>? = null,
        val sticker: Sticker? = null,
        val video: Video? = null,
        val video_note: VideoNote? = null,
        val voice: Voice? = null,
        val caption: String? = null,
        val caption_entities: List<MessageEntity>? = null,
        val contact: Contact? = null,
        val dice: Dice? = null,
        val game: Game? = null,
        val poll: Poll? = null,
        val venue: Venue? = null,
        val location: Location? = null,
        val new_chat_members: List<User>? = null,
        val left_chat_member: User? = null,
        val new_chat_title: String? = null,
        val new_chat_photo: List<PhotoSize>? = null,
        val delete_chat_photo: Boolean? = null,
        val group_chat_created: Boolean? = null,
        val supergroup_chat_created: Boolean? = null,
        val channel_chat_created: Boolean? = null,
        val migrate_to_chat_id: Long? = null,
        val pinned_message: Message? = null,
        val invoice: Invoice? = null,
        val successful_payment: SuccessfulPayment? = null,
        val connected_website: String? = null,
        val passport_data: PassportData? = null,
        val reply_markup: InlineKeyboardMarkup? = null
)