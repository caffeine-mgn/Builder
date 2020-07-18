package pw.binom.builder.master.taskStorage.file

import pw.binom.builder.master.taskStorage.TaskStorage
import pw.binom.io.file.File
import pw.binom.io.file.read
import pw.binom.io.file.write
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.io.utf8Appendable
import pw.binom.io.utf8Reader

class BuildFile(val file: File, override val job: TaskStorage.Job) : TaskStorage.Build {
    override val number: Int
        get() {
            val i = file.path.lastIndexOf('/')
            return if (i == -1)
                file.path.toInt()
            else
                file.path.substring(i + 1).toInt()
        }

    private val data by lazy {
        File(file, "build.json").read().utf8Reader().use {
            taskStorageJsonSerialization.parse(BuildDto.serializer(), it.readText())
        }
    }

    private val outputFile = File(file, "output.txt")

    override val status: TaskStorage.BuildStatus
        get() = when (data.status) {
            BuildDto.BuildStatus.PREPARE -> TaskStorage.BuildStatus.PREPARE
            BuildDto.BuildStatus.PROCESS -> TaskStorage.BuildStatus.PROCESS
            BuildDto.BuildStatus.FINISHED_OK -> TaskStorage.BuildStatus.FINISHED_OK
            BuildDto.BuildStatus.FINISHED_ERROR -> TaskStorage.BuildStatus.FINISHED_ERROR
            BuildDto.BuildStatus.CANCELED -> TaskStorage.BuildStatus.CANCELED
        }

    override fun addStdout(text: String) {
        outputFile.write(true).utf8Appendable().use {
            it.append("STDOUT:$text")
        }
    }

    override fun addStderr(text: String) {
        outputFile.write(true).utf8Appendable().use {
            it.append("STDERR:$text")
        }
    }

}