package pw.binom.builder.master.taskStorage.file

import pw.binom.builder.map
import pw.binom.builder.master.taskStorage.TaskStorage
import pw.binom.io.file.File
import pw.binom.io.file.iterator
import pw.binom.io.file.read
import pw.binom.io.file.write
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.io.utf8Appendable
import pw.binom.io.utf8Reader

class JobFile(override val file: File, override val taskStorage: TaskStorageFile) : TaskStorage.Job, AbstractEntityHolderFile() {
    private val data by lazy {
        File(file, "job.json").read().utf8Reader().use {
            taskStorageJsonSerialization.parse(JobDto.serializer(), it.readText())
        }
    }

    override fun getBuild(build: Int): TaskStorage.Build? {
        val f = File(file, build.toString())
        if (!f.isDirectory)
            return null

        return BuildFile(f, this)
    }

    override fun getBuilds(): List<TaskStorage.Build> =
            file.iterator().use {
                it.map {
                    BuildFile(it, this)
                }
            }

    override fun createBuild(): TaskStorage.Build {
        val num = data.nextBuild
        val f = File(file, num.toString())
        f.mkdir()
        File(f, "build.json").write().utf8Appendable().use {
            it.append(taskStorageJsonSerialization.stringify(BuildDto.serializer(), BuildDto(BuildDto.BuildStatus.PREPARE)))
        }
        File(f, "output.txt").write().close()

        data.nextBuild = num + 1
        File(file, "job.json").write().utf8Appendable().use {
            it.append(taskStorageJsonSerialization.stringify(JobDto.serializer(), data))
        }

        return BuildFile(f, this)
    }

    override val config: TaskStorage.JobConfig
        get() = TaskStorage.JobConfig(
                cmd = data.cmd,
                exclude = data.exclude,
                include = data.include,
                env = data.env
        )
}