package pw.binom.builder.web

import org.tlsys.css.CSS
import pw.binom.builder.remote.*
import pw.binom.io.UTF8
import kotlin.browser.window
import kotlin.dom.addClass

private val StatusStyle = CSS.style {
    color = "#eaeaea"
    fontFamily = Styles.DEFAULT_FONT
    paddingLeft = "15px"
    fontSize = "12px"
}.name

class TaskPage(val path: String, var job: JobInformation) : Page() {
    private val pathDecode = decodeURIComponent(path)
    override suspend fun getTitle(): String = getNameFromPath(pathDecode)

    val builds by lazy { async { Client.taskManager.getJobBuildings(pathDecode) } }

    private val toolbar = Toolbar().appendTo(layout, grow = 0, shrink = 0).addSpace()
    private val startJob = Button("Run").appendTo(toolbar)
    private val editBashJob = Button("Edit Bash").appendTo(toolbar)
    private val buildsList = ListView<Item>().appendTo(layout)

    init {
        startJob.click {
            async {
                val exe = Client.processService.execute(pathDecode)
                EventNotification.add("Job ${exe.path} created. Build Number: ${exe.buildNumber}")
                buildsList.addFirst(Item(JobStatus(process = exe, status = JobStatusType.PREPARE.name, end = null, start = null)))
            }
        }
        editBashJob.click {
            PageNavigator.goto("${window.location.href}/bash")
        }

        subscribe {
            EventBus.wait { e ->
                if (e is Event_TaskStatusChange && e.process.path == pathDecode.removePrefix("/").decodeUrl()) {
                    buildsList.asSequence()
                            .filter { it.status.process.buildNumber == e.process.buildNumber }
                            .forEach {
                                it.update(e.statusEnum)
                            }
                }
            }
        }
    }

    private inner class Item(var status: JobStatus) : DivComponentWithLayout() {
        private val link = LinkPlace().appendTo(layout, grow = 1, shrink = 1)
        val span = Span().appendTo(link.layout, grow = 0, shrink = 0)

        val spanStatus = Span().appendTo(layout, grow = 0, shrink = 0)
        val cancel = Button("Cancel").appendTo(layout, grow = 0, shrink = 0)

        private fun update() {
            span.text = status.process.buildNumber.toString()
            spanStatus.text = status.statusEnum?.name?.let { "[$it]" } ?: ""
        }

        fun update(status: JobStatusType) {
            this.status = this.status.newStatus(status)
            update()
        }

        init {
            layout.alignItems = FlexLayout.AlignItems.Center
            dom.addClass(Styles.LIST_ITEM)
            span.dom.addClass(Styles.SIMPLE_TEXT)
            spanStatus.dom.addClass(StatusStyle)
            link.href = "$uiUrl/tasks/${status.process.path.taskUrl()}/b${status.process.buildNumber}"

            cancel.click {
                async {
                    Client.processService.cancel(status.process)
                }
            }

//            dom.onclick = {
//                PageNavigator.goto(link.href)
//                it.preventDefault()
//            }
            update()
        }
    }

    override suspend fun onInit() {
        super.onInit()
        builds.await().sortedBy { -it.process.buildNumber }.forEach {
            buildsList.addLast(Item(it))
        }
    }

    override suspend fun next(page: String): Page? {
        if (page == "bash")
            return EditBashPage(this.path, job)
        if (page.startsWith("b")) {
            return BuildPage(
                    JobProcess(
                            buildNumber = page.removePrefix("b").toLong(),
                            path = path
                    )
            )
        }
        return super.next(page)
    }
}