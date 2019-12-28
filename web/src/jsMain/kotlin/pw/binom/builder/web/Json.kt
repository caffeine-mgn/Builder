package pw.binom.builder.web

import kotlin.js.Json

private class JEntry<K, V>(override val key: K, override val value: V) : Map.Entry<K, V>

fun Json.forEach(func: (Map.Entry<String, Any?>) -> Unit) {
    val obj = this
    js("Object.keys(obj)").unsafeCast<Array<String>>().forEach {
        if (jsTypeOf(this[it]) == "function")
            return@forEach

        func(JEntry(it, this[it]))
    }
}

val <T : Any>T.asJson: Json
    get() = this.unsafeCast<Json>()