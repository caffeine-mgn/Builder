package pw.binom.builder.server
/*
import pw.binom.Date
import pw.binom.builder.OutType
import pw.binom.builder.filter
import pw.binom.builder.map
import pw.binom.builder.remote.*
import pw.binom.builder.withCounter
import pw.binom.io.*
import pw.binom.io.file.*
import pw.binom.thread.Thread


class TaskManager1(val root: File) {
    abstract inner class Entity(val file: File) {
        val name: String
            get() = file.name

        val path: String
            get() = file.path.removePrefix(root.path).replace('\\', '/').removePrefix("/").removePrefix("/")

        abstract fun toTaskItem(): TaskItem
    }

    inner class Dir(file: File) : Entity(file) {
        override fun toTaskItem() =
                TaskItem(path = path, name = name, isTask = false)

        suspend fun createJob(name: String, job: JobInformation): Job {
            val file = File(File(file, name), "job.json")
            file.parent!!.mkdir()

            JobFile.new(file).save(job)
            return Job(file.parent!!)
        }

        fun createDir(name: String): Dir {
            val file = File(file, name)
            file.mkdirs()
            return Dir(file)
        }
    }

    inner class Job(file: File) : Entity(file) {

        private val statusFile = "status.txt"
        private val startedFile = "started.txt"
        private val stoppedFile = "stopped.txt"

        fun getOutputFirst(buildNum: Long, length: Long): Reader? {
            val file = File(buildNum(buildNum), "out.txt")
            if (!file.isFile)
                return null

            return FileInputStream(file).maxRead(length).utf8Reader()
        }

        /**
         * Returns last lines where bytes size less or equal length
         *
         * @param buildNum number of build
         * @param length size of bytes
         * @return result. If output file not exist will return null
         */
        fun getOutputLast(buildNum: Long, length: Int): LastOutDto? {
            val file = File(buildNum(buildNum), "out.txt")
            if (!file.isFile)
                return null

            return if (file.size <= length) {
                LastOutDto(
                        text = FileInputStream(file).utf8Reader().use { it.readText() },
                        skipped = 0
                )
            } else {
                val position = file.size - length
                FileInputStream(file).withCounter().use {
                    it.skip(position)
                    while (true) {
                        try {
                            val b = it.read()
                            if (b == 10.toByte() || b == 13.toByte()) {
                                break
                            } else {
                                println("$b (${b.toChar()})")
                            }
                        } catch (e: EOFException) {
                            break
                        }
                    }
                    LastOutDto(
                            skipped = it.counter,
                            text = it.utf8Reader().use { it.readText() }
                    )
                }
            }
        }

        fun getBuilds(): List<JobStatus> =
                file.iterator().use {
                    it.filter { it.isDirectory }.map {
                        val buildNum = it.name.toLong()
                        JobStatus(
                                process = JobProcess(buildNumber = buildNum, path = path),
                                start = getStarted(buildNum),
                                end = getEnd(buildNum),
                                status = getStatus(buildNum).type.name
                        )
                    }
                }

        fun finish(buildNum: Long, ok: Boolean) {
            FileOutputStream(File(buildNum(buildNum), statusFile)).use {
                val type = if (ok) JobStatusType.FINISHED_OK else JobStatusType.FINISHED_ERROR
                it.utf8Appendable().append(type.name).append("\n").append(Thread.currentTimeMillis().toString()).append("\n")
            }
        }

        fun cancel(buildNum: Long) {
            FileOutputStream(File(buildNum(buildNum), statusFile)).use {
                val type = JobStatusType.CANCELED
                it.utf8Appendable().append(type.name).append("\n").append(Thread.currentTimeMillis().toString()).append("\n")
            }
        }

        fun start(buildNum: Long) {
            val file = File(buildNum(buildNum), startedFile)
            FileOutputStream(file).use {
                it.utf8Appendable().append(Date.now().time.toString())
            }
            FileOutputStream(File(buildNum(buildNum), statusFile)).use {
                it.utf8Appendable().append(JobStatusType.PROCESS.name).append("\n").append(Thread.currentTimeMillis().toString()).append("\n")
            }
        }

//        private fun process(buildNum: Long) {
//            FileOutputStream(File(buildNum(buildNum), statusFile)).use {
//                it.utf8Appendable().append(JobStatusType.PROCESS.name).append("\n").append(Thread.currentTimeMillis().toString()).append("\n")
//            }
//        }

        fun getStarted(buildNum: Long): Long? {
            val file = File(buildNum(buildNum), startedFile)
            if (!file.isExist)
                return null
            return FileInputStream(file).use {
                it.utf8Reader().readText()
            }.toLong()
        }

        fun getEnd(buildNum: Long): Long? {
            val file = File(buildNum(buildNum), stoppedFile)
            if (!file.isExist)
                return null
            return FileInputStream(file).use {
                it.utf8Reader().readText()
            }.toLong()
        }

        fun getStatus(buildNum: Long): Status {
            val f = File(buildNum(buildNum), statusFile)
            if (!f.isFile) {
                return Status(JobStatusType.PREPARE, Thread.currentTimeMillis())
            }

            FileInputStream(f).use {
                val r = it.utf8Reader()
                val type = JobStatusType.valueOf(r.readln())
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
            FileOutputStream(File(buildNum(buildNum), "out.txt"), true).use {
                it.utf8Appendable().append(OutType.STDOUT.name).append(":").append(txt).append("\n")
            }
        }

        fun writeStderr(buildNum: Long, txt: String) {
            FileOutputStream(File(buildNum(buildNum), "out.txt"), true).use {
                it.utf8Appendable().append(OutType.STDERR.name).append(":").append(txt).append("\n")
            }
        }

        suspend fun jobFile() = JobFile.open(File(file, "job.json"))

        fun toExecuteJob(buildNumber: Long) = JobProcess(path = path, buildNumber = buildNumber)
        override fun toTaskItem() =
                TaskItem(path = path, name = name, isTask = true)
    }

    fun getJob(path: String): Job? {
        val file = File(root, path)
        if (!file.isJob()) {
            return null
        }
        return Job(file)
    }

    fun getDir(path: String): Dir? {
        val file = File(root, path)
        if (!file.isDir()) {
            return null
        }
        return Dir(file)
    }

    fun getPath(path: String): List<Entity>? {
        val file = File(root, path)
        if (!file.isDirectory)
            return null
        if (!file.isDir())
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
*/