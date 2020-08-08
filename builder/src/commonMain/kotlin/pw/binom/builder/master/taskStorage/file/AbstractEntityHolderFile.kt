package pw.binom.builder.master.taskStorage.file

import pw.binom.builder.map
import pw.binom.builder.master.taskStorage.EntityHolder
import pw.binom.builder.master.taskStorage.TaskStorage
import pw.binom.io.file.*
import pw.binom.io.use
import pw.binom.io.utf8Appendable

abstract class AbstractEntityHolderFile : EntityHolder, TaskStorage.Entity {

    abstract val file: File
    abstract val taskStorage: TaskStorageFile

    override val path: String
        get() = file.path.removePrefix("${taskStorage.file.path}/")

    override fun getEntity(path: String): TaskStorage.Entity? {
        require("/" !in path)
        val f = File(file, path)
        if (!f.isExist)
            return null
        println("${f} isJob=${f.isJob}")
        return if (f.isJob) {
            println("Returns job")
            JobFile(f, taskStorage)
        } else {
            println("Returns direction")
            DirectionFile(f, taskStorage)
        }
    }

    override fun getEntityList(): List<TaskStorage.Entity>? {
        return file.iterator().use { it ->
            it.map {
                when {
                    it.isJob -> JobFile(it, taskStorage)
                    it.isDirectory -> DirectionFile(it, taskStorage)
                    else -> null
                }
            }.filterNotNull()
        }
    }

    override fun createJob(name: String, config: TaskStorage.JobConfig): TaskStorage.Job {
        require(path !in "/")
        val dto = JobDto(
                cmd = config.cmd,
                env = config.env.toMutableMap(),
                include = config.include.toMutableSet(),
                exclude = config.exclude.toMutableSet(),
                nextBuild = 1
        )
        val jobText = taskStorageJsonSerialization.stringify(JobDto.serializer(), dto)
        val dir = File(file, name)
        File(dir, "job.json").write().utf8Appendable().use {
            it.append(jobText)
        }
        return JobFile(dir, taskStorage)
    }

    override fun createDirection(name: String): TaskStorage.Direction {
        require(path !in "/")
        val f = File(file, name)
        f.mkdirs()
        return DirectionFile(f, taskStorage)
    }

}

private val File.isJob
    get() = File(this, "job.json").isFile