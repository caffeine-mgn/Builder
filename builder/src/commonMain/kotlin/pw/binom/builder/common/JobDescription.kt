package pw.binom.builder.common

import pw.binom.Platform
import pw.binom.io.AsyncAppendable
import pw.binom.io.AsyncReader
import pw.binom.json.*
/*
class JobDescription(val buildNumber: Long, val path: String, val cmd: String, val env: Map<String, String>, val platform: Platform?) {

    suspend fun write(appendable: AsyncAppendable) {
        jsonNode(appendable) {
            number("buildNumber", buildNumber)
            string("path", path)
            string("cmd", cmd)
            if (platform == null)
                attrNull("platform")
            else
                string("platform", platform.name)
            node("env") {
                env.forEach { i ->
                    string(i.key, i.value)
                }
            }
        }
    }

    fun toExecuteJob() = ExecuteJob(buildNumber = buildNumber, path = path)

    companion object {
        suspend fun read(reader: AsyncReader): JobDescription {
            val r = JsonDomReader()
            JsonReader(reader).accept(r)
            val buildNumber = r.node.obj["buildNumber"]!!.long
            val path = r.node.obj["path"]!!.text
            val cmd = r.node.obj["cmd"]!!.text
            val env = HashMap<String, String>()
            val platform = r.node.obj["platform"]?.let { Platform.valueOf(it.text) }
            r.node.obj["env"]?.obj?.forEach {
                env[it.key] = it.value!!.text
            }
            return JobDescription(buildNumber = buildNumber, path = path, cmd = cmd, env = env, platform = platform)
        }
    }
}
*/