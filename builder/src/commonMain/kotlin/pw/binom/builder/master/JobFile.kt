package pw.binom.builder.master
/*
import pw.binom.builder.remote.EnvVar
import pw.binom.builder.remote.JobInformation
import pw.binom.io.asAsync
import pw.binom.io.file.File
import pw.binom.io.file.FileInputStream
import pw.binom.io.file.FileOutputStream
import pw.binom.io.use
import pw.binom.io.utf8Appendable
import pw.binom.io.utf8Reader
import pw.binom.json.*

class JobFile private constructor(
        val file: File,
        val cmd: String,
        val nextBuild: Long,
        val include: List<String>,
        val exclude: List<String>,
        val env: Map<String, String>) {

    suspend fun save(job: JobInformation) =
            JobFile(
                    file = file,
                    cmd = job.cmd,
                    nextBuild = nextBuild,
                    env = job.env.associate { it.name to it.value },
                    include = job.include,
                    exclude = job.exclude
            ).save()


    private suspend fun save(): JobFile {
        FileOutputStream(file).use {
            jsonNode {
                string("cmd", cmd)
                number("nextBuild", nextBuild)

                array("include", include.map { JsonString(it) }.toJsonArray())
                array("exclude", exclude.map { JsonString(it) }.toJsonArray())

                node("env") {
                    env.forEach { i ->
                        string(i.key, i.value)
                    }
                }
            }.write(it.asAsync().utf8Appendable())
            it.flush()
        }
        return this
    }

    suspend fun newNextBuild(nextBuild: Long) = JobFile(file = file, cmd = cmd, nextBuild = nextBuild, env = env, exclude = exclude, include = include).save()
    suspend fun incNextBuild() = newNextBuild(nextBuild + 1)
    suspend fun newCmd(cmd: String) = JobFile(file = file, cmd = cmd, nextBuild = nextBuild, env = env, exclude = exclude, include = include).save()
    fun toJobInformation()=JobInformation(
            cmd=cmd,
            env = env.map { EnvVar(it.key,it.value) },
            include = include,
            exclude = exclude
    )
    /*
    suspend fun toJobInformation() = JobInformation(
            cmd = cmd,
            platform = platform,
            env = env
    )
     */

    companion object {
        suspend fun new(file: File): JobFile {
            val jobFile = JobFile(file = file, include = emptyList(), exclude = emptyList(), env = emptyMap(), cmd = "", nextBuild = 1)
            jobFile.save()
            return jobFile
        }

        suspend fun open(file: File): JobFile {
            var node: JsonNode? = null
            val reader = JsonDomReader {
                node = it
            }
            FileInputStream(file).use {
                JsonReader(it.utf8Reader().asAsync()).accept(reader)
            }

            val json = node ?: JsonObject()
            val cmd = json.obj["cmd"]?.string ?: ""
            val nextBuild = json.obj["nextBuild"]?.long ?: 1L
            val include = json.obj["include"]?.array?.filterNotNull()?.map { it.string } ?: emptyList()
            val exclude = json.obj["exclude"]?.array?.filterNotNull()?.map { it.string } ?: emptyList()

            val env = HashMap<String, String>()
            json.obj["env"]?.obj?.forEach {
                env[it.key] = it.value!!.string
            }

            return JobFile(file = file, cmd = cmd, nextBuild = nextBuild, env = env, include = include, exclude = exclude)
        }
    }
}

 */