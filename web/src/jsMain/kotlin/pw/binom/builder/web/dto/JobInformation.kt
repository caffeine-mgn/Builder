package pw.binom.builder.web.dto

import pw.binom.builder.web.asJson
import pw.binom.builder.web.forEach
import kotlin.js.Json

/*
/**
 * Описание задачи. Используется для редактирования задачи
 */
class JobInformation(val cmd: String, val env: Map<String, String>, val platform: Platform?) {
    suspend fun write(): Json {
        val j = js("({})").unsafeCast<Json>()
        j["cmd"] = cmd
        if (platform == null)
            j["platform"] = null
        else
            j["platform"] = platform.name

        val e = js("({})").unsafeCast<Json>()
        j["env"] = e

        env.forEach {
            env.forEach { i ->
                e[i.key] = i.value
            }
        }
        return j
    }

    companion object {
        fun read(node: Json): JobInformation {
            val cmd = node["cmd"].unsafeCast<String>()
            val env = HashMap<String, String>()
            val platform = node["platform"]?.let { Platform.valueOf(it.unsafeCast<String>()) }
            node["env"]?.asJson?.forEach {
                env[it.key] = it.value.unsafeCast<String>()
            }
            return JobInformation(cmd = cmd, env = env, platform = platform)
        }
    }
}
*/