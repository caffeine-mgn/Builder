package pw.binom.builder.server

import pw.binom.Platform
import pw.binom.io.asAsync
import pw.binom.io.file.File
import pw.binom.io.file.FileInputStream
import pw.binom.io.file.FileOutputStream
import pw.binom.io.use
import pw.binom.io.utf8Appendable
import pw.binom.io.utf8Reader
import pw.binom.json.*

class JobFile private constructor(val file: File, val cmd: String, val nextBuild: Long, val platform: Platform?, val env: Map<String, String>) {

    private suspend fun save(): JobFile {
        FileOutputStream(file).use {
            jsonNode(it.utf8Appendable().asAsync()) {
                string("cmd", cmd)
                number("nextBuild", nextBuild)

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
            it.flush()
        }
        return this
    }

    suspend fun newNextBuild(nextBuild: Long) = JobFile(file = file, cmd = cmd, nextBuild = nextBuild, env = env, platform = platform).save()
    suspend fun incNextBuild() = newNextBuild(nextBuild + 1)
    suspend fun newCmd(cmd: String) = JobFile(file = file, cmd = cmd, nextBuild = nextBuild, env = env, platform = platform).save()

    companion object {
        suspend fun open(file: File): JobFile {
            var node: JsonNode? = null
            val reader = JsonDomReader {
                node = it
            }
            FileInputStream(file).use {
                JsonReader(it.utf8Reader().asAsync()).accept(reader)
            }

            val json = node ?: JsonObject()
            val cmd = json.obj["cmd"]?.text ?: ""
            val nextBuild = json.obj["nextBuild"]?.long ?: 1L
            val platform = json.obj["platform"]?.takeIf { !it.isNull }?.let { Platform.valueOf(it.text) }

            val env = HashMap<String, String>()
            json.obj["env"]?.obj?.forEach {
                env[it.key] = it.value.text
            }

            return JobFile(file = file, cmd = cmd, nextBuild = nextBuild, env = env, platform = platform)
        }
    }
}