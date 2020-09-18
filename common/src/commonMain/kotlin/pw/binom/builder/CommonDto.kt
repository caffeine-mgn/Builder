package pw.binom.builder

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

private val dtoModule24 = SerializersModule {
    this.polymorphic(CommonDto::class, CommonDto::class, CommonDto.serializer())
}

private val commonJsonSerialization = Json{
    this.classDiscriminator="@class"
    this.serializersModule=dtoModule24
}

@Serializable
sealed class CommonDto {
    fun toJson(): String = commonJsonSerialization.encodeToString(serializer(), this)

    companion object {
        fun toResponse(json: String): CommonDto = commonJsonSerialization.decodeFromString(serializer(), json)
    }
}