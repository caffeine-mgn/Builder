package pw.binom.builder.common

import pw.binom.Platform
import pw.binom.json.*

/*
/**
 * Описание задачи. Используется для редактирования задачи
 */
class JobInformation(val cmd: String, val env: Map<String, String>, val platform: Platform?) {
    suspend fun write(ctx: ObjectCtx) {
        ctx.run {
            string("cmd", cmd)
            if (platform == null)
                nil("platform")
            else
                string("platform", platform.name)
            node("env") {
                env.forEach { i ->
                    string(i.key, i.value)
                }
            }
        }
    }

    companion object {
        fun read(node: JsonNode): JobInformation {
            val cmd = node.obj["cmd"]!!.string
            val env = HashMap<String, String>()
            val platform = node.obj["platform"]?.let { Platform.valueOf(it.string) }
            node.obj["env"]?.obj?.forEach {
                env[it.key] = it.value!!.string
            }
            return JobInformation(cmd = cmd, env = env, platform = platform)
        }
    }
}
*/