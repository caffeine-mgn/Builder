package pw.binom.builder

import pw.binom.io.AsyncAppendable
import pw.binom.io.AsyncReader
import pw.binom.json.*
/*
suspend fun AsyncReader.readJson(): JsonNode {
    val r = JsonDomReader()
    JsonReader(this).accept(r)
    return r.node
}
*/
suspend fun AsyncAppendable.append(jsonNode: JsonNode) {
    jsonNode.accept(JsonWriter(this))
}

fun jsonArrayOf(vararg values: JsonNode?): JsonArray =
        jsonArrayOf(values.toList())

fun jsonArrayOf(values: List<JsonNode?>): JsonArray {
    val array = JsonArray()
    array += values
    return array
}