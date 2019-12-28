package pw.binom.builder.web

import pw.binom.builder.remote.JobProcess
import pw.binom.io.Closeable

class BuildPage(val executeJob: JobProcess) : Page() {
    override suspend fun getTitle(): String = executeJob.buildNumber.toString()

    private val outputView = OutputView().appendTo(layout)

    private var listener: Closeable? = null

    override suspend fun onStart() {
        super.onStart()
        val out = Client.taskManager.getLastOutput(executeJob,1024 * 1024)//API.getTaskOutputLast(path = executeJob.path, buildNumber = executeJob.buildNumber, size = 1024 * 1024)
        if (out.skipped > 0)
            outputView.setDownloadfull(out.skipped) {
                API.getTaskOutputFirst(path = executeJob.path, buildNumber = executeJob.buildNumber, size = out.skipped)
            }
        out.text.lineSequence().forEach {
            outputView.append(it)
        }
        outputView.moveDown()
        listener = Client.tail(executeJob) {
            if (it == null) {
                console.info("Task done!")
                listener = null
                return@tail
            }
            outputView.append(it)
            outputView.moveDown()
        }
    }

    override suspend fun onStop() {
        listener?.close()
        listener = null
        outputView.clear()
        super.onStop()
    }
}