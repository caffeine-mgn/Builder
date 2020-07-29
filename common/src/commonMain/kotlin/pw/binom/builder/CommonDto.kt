package pw.binom.builder

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

@OptIn(ImplicitReflectionSerializer::class)
private val dtoModule24 = SerializersModule {
    this.polymorphic(CommonDto::class.serializer())
}

private val commonJsonSerialization = Json(JsonConfiguration.Stable.copy(
        classDiscriminator = "@class"
), dtoModule24)

@Serializable
sealed class CommonDto {
    fun toJson(): String = commonJsonSerialization.stringify(serializer(), this)

    companion object {
        fun toResponse(json: String): CommonDto = commonJsonSerialization.parse(serializer(), json)
    }
}