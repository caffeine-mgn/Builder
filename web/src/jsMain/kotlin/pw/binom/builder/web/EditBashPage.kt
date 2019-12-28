package pw.binom.builder.web

import pw.binom.builder.remote.JobInformation

class EditBashPage(val path: String, var job: JobInformation) : Page() {
    override suspend fun getTitle(): String = "Edit Bash"

    private val toolPanel = Toolbar().appendTo(layout, grow = 0, shrink = 0).addSpace()
    private val saveBtn = Button("Save").appendTo(toolPanel)
    private val bashEditor = BashEditor(source = job.cmd).appendTo(layout)

    private fun update() {
        job = JobInformation(
                cmd = bashEditor.source,
                env = job.env,
                include = job.include,
                exclude = job.exclude
        )
    }

    private fun save() = async {
        update()
        job=Client.taskManager.updateJob(path, job)!!
        EventNotification.add("Job $path saved")
    }

    init {
        saveBtn.click {
            save()
        }

        keyDownEvent { e ->
            if (e.keyCode == 83 && e.ctrlKey) {
                save()
                e.preventDefault()
            }
        }
    }
}