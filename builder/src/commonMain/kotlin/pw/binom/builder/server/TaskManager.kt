package pw.binom.builder.server

import pw.binom.Date
import pw.binom.Thread
import pw.binom.builder.common.ExecuteJob
import pw.binom.io.*
import pw.binom.io.file.*

class TaskManager(val root: File) {
    abstract inner class Entity(val file: File) {
        val name: String
            get() = file.name

        val path: String
            get() = file.path.removePrefix(root.path).replace('\\', '/').removePrefix("/").removePrefix("/")
    }

    inner class Dir(file: File) : Entity(file)

    inner class Job(file: File) : Entity(file) {

        private val statusFile = "status.txt"
        private val startedFile = "started.txt"
        private val stoppedFile = "stopped.txt"
        fun finish(buildNum: Long, ok: Boolean) {
            FileOutputStream(File(buildNum(buildNum), statusFile)).use {
                val type = if (ok) Status.Type.FINISHED_OK else Status.Type.FINISHED_ERROR
                it.utf8Appendable().append(type.name).append("\n").append(Thread.currentTimeMillis().toString()).append("\n")
            }
        }

        fun start(buildNum: Long) {
            val file = File(buildNum(buildNum), startedFile)
            FileOutputStream(file).use {
                it.utf8Appendable().append(Date.now().time.toString())
            }
            FileOutputStream(File(buildNum(buildNum), statusFile)).use {
                it.utf8Appendable().append(Status.Type.PREPARE.name).append("\n").append(Thread.currentTimeMillis().toString()).append("\n")
            }
        }

        private fun process(buildNum: Long) {
            FileOutputStream(File(buildNum(buildNum), statusFile)).use {
                it.utf8Appendable().append(Status.Type.PROCESS.name).append("\n").append(Thread.currentTimeMillis().toString()).append("\n")
            }
        }

        fun getStarted(buildNum: Long): Long? {
            val file = File(buildNum(buildNum), startedFile)
            return FileInputStream(file).use {
                it.utf8Reader().readText()
            }.toLong()
        }

        private fun getStatus(buildNum: Long): Status {
            val f = File(buildNum(buildNum), statusFile)
            if (!f.isFile) {
                return Status(Status.Type.PREPARE, Thread.currentTimeMillis())
            }

            FileInputStream(f).use {
                val r = it.utf8Reader()
                val type = Status.Type.valueOf(r.readln())
                val time = r.readln().toLong()
                return Status(type, time)
            }
        }

        fun buildNum(number: Long): File {
            val f = File(file, number.toString())
            f.mkdirs()
            return f
        }

        fun writeStdout(buildNum: Long, txt: String) {
            process(buildNum)
            FileOutputStream(File(buildNum(buildNum), "stdout.txt"), true).use {
                it.utf8Appendable().append(Output.STDOUT.name).append(":").append(txt).append("\n")
            }
        }

        fun writeStderr(buildNum: Long, txt: String) {
            process(buildNum)
            FileOutputStream(File(buildNum(buildNum), "stdout.txt"), true).use {
                it.utf8Appendable().append(Output.STDERR.name).append(":").append(txt).append("\n")
            }
        }

        suspend fun jobFile() = JobFile.open(File(file, "job.json"))

        fun toExecuteJob(buildNumber: Long) = ExecuteJob(path = path, buildNumber = buildNumber)
    }

    fun getJob(path: String): Job? {
        val file = File(root, path)
        if (!file.isJob()) {
            return null
        }
        return Job(file)
    }

    fun getPath(path: String): List<Entity>? {
        val file = File(root, path)
        if (!file.isDirectory)
            return null
        val out = ArrayList<Entity>()
        file.iterator().use {
            it.forEach {
                if (it.isJob())
                    out += Job(it)
                if (it.isDir())
                    out += Dir(it)
            }
        }

        return out
    }

    private fun File.isJob(): Boolean {
        if (!this.isDirectory) {
            return false
        }

        val jobFile = File(this, "job.json")
        return jobFile.isFile
    }

    private fun File.isDir(): Boolean {
        if (!this.isDirectory)
            return false
        return !isJob()
    }
}