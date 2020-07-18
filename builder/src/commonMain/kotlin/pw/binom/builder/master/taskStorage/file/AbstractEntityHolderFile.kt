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
        get() = file.path.removePrefix(taskStorage.file.path)

    override fun getEntity(path: String): TaskStorage.Entity? {
        require(path !in "/")
        val f = File(file, path)
        return if (file.isJob) {
            DirectionFile(f, taskStorage)
        } else {
            JobFile(f, taskStorage)
        }
    }

    override fun getEntityList(): List<TaskStorage.Entity>? {
        return file.iterator().use { it ->
            it.map {
                if (it.isJob)
                    DirectionFile(it, taskStorage)
                else
                    JobFile(it, taskStorage)
            }
        }
    }

    override fun createJob(name: String, config: TaskStorage.JobConfig) {
        require(path !in "/")
        val jobText = taskStorageJsonSerialization.stringify(JobDto.serializer(), JobDto(
                cmd = config.cmd,
                env = config.env,
                include = config.include,
                exclude = config.exclude,
                nextBuild = 1
        ))
        File(file, name).write().utf8Appendable().use {
            it.append(jobText)
        }
    }

    override fun createDirection(name: String) {
        require(path !in "/")
        File(file, name).mkdirs()
    }

}

private val File.isJob
    get() = File(this, "job.json").isFile