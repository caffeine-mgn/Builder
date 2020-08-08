package pw.binom.builder.web

import pw.binom.builder.Event
import pw.binom.builder.dto.Entity
import pw.binom.builder.web.services.EventService
import pw.binom.builder.web.services.TaskSchedulerService
import pw.binom.builder.web.services.TasksService
import pw.binom.io.Closeable
import kotlin.dom.clear

class TasksPage(val path: String) : Page() {
    override suspend fun getTitle(): String =
            if (path.isEmpty())
                "Tasks"
            else
                "Tasks ${path}"

    private val list = DivLayout(direction = FlexLayout.Direction.Column).appendTo(layout)
    private val scroll = ScrollController(list.dom)

    override suspend fun onStart() {
        super.onStart()
        TasksService.getList(path)!!.forEach {
            when (it) {
                is Entity.Job -> TaskItem(it).appendTo(list.layout, grow = 0, shrink = 0)
                is Entity.Direction -> TasksboxItem(it).appendTo(list.layout, grow = 0, shrink = 0)
            }
        }
    }

    private abstract class AbstractItem : DivComponentWithLayout(direction = FlexLayout.Direction.Column) {
        protected val body = DivLayout(direction = FlexLayout.Direction.Row).appendTo(layout, grow = 0, shrink = 0)
    }

    private class TaskItem(task: Entity.Job) : AbstractItem() {
        var task: Entity.Job = task
            set(value) {
                field = value
                refresh()
            }

        private val childs = DivLayout(direction = FlexLayout.Direction.Column).appendTo(layout)

        private var listener: Closeable? = null

        private fun refresh() {
            title.text = task.name
//            run.dom.style.display = if (task.worker == null) "" else "none"
//            cancel.dom.style.display = if (task.worker == null) "none" else ""
        }

        val title = Span().appendTo(body.layout)
        val run = Button("Run").appendTo(body.layout, grow = 0, shrink = 0)
        private val builds = HashMap<Entity.Job.Build, BuildItem>()

        override suspend fun onStart() {
            super.onStart()
            listener = EventService.addListener { e ->
                if (e is Event.TaskChangeStatus) {
                    if (e.path != task.path)
                        return@addListener
                    console.info("${task.path} Event: $e")
                    val build = task.builds.find { it.buildNumber == e.buildNumber }
                    if (build == null) {
                        if (!e.status.terminateState) {
                            val b = Entity.Job.Build(
                                    worker = e.worker,
                                    buildNumber = e.buildNumber,
                                    status = e.status
                            )
                            task.builds += b
                            val item = BuildItem(b).appendTo(childs.layout, grow = 0, shrink = 0)
                            builds[b] = item
                        }
                    } else {
                        if (e.status.terminateState) {
                            task.builds.remove(build)
                            builds.remove(build)?.dom?.remove()
                        } else {
                            build.buildNumber = e.buildNumber
                            build.status = e.status
                            build.worker = e.worker
                            builds[build]?.update()
                        }
                    }
                }
            }
        }

        override suspend fun onStop() {
            listener?.close()
            console.info("Stop ${task.path}")
            super.onStop()
        }

        init {
            childs.dom.style.paddingLeft = "15px"
            refresh()
            run.click {
                async {
                    TaskSchedulerService.run(task.path)
                }
            }
        }
    }

    private class BuildItem(val build: Entity.Job.Build) : DivComponentWithLayout(direction = FlexLayout.Direction.Row) {
        val status = Span().appendTo(layout)

        fun update() {
            status.text = build.status.name
        }

        init {
            update()
        }
    }

    private class TasksboxItem(task: Entity.Direction) : AbstractItem() {
        var task: Entity.Direction = task
            set(value) {
                field = value
                refresh()
            }
        private val childs = DivLayout(direction = FlexLayout.Direction.Column).appendTo(layout)

        private fun refresh() {
            title.text = task.name
        }

        val title = Span().appendTo(body.layout)
        private var expand = false

        init {
            childs.dom.style.paddingLeft = "15px"
            refresh()
            title.dom.onclick = {
                expand = !expand
                if (!expand) {
                    async {
                        childs.onStop()
                        childs.dom.clear()
                        childs.dom.style.display = "none"
                    }
                } else {
                    async {
                        TasksService.getList(task.path)!!.forEach {
                            when (it) {
                                is Entity.Job -> TaskItem(it).appendTo(childs.layout, grow = 0, shrink = 0)
                                is Entity.Direction -> TasksboxItem(it).appendTo(childs.layout, grow = 0, shrink = 0)
                            }
                        }
                        childs.onStart()
                        childs.dom.style.display = ""
                    }
                }
            }
        }
    }
}

/*
import pw.binom.builder.remote.TaskItem
import pw.binom.io.UTF8
import kotlin.dom.addClass

fun nextJobItem(parent: String, child: String): String {
    if (parent.isEmpty())
        return child
    return "$parent/$child"
}

class TasksPage(val path: String) : Page() {
    private val pathDecode = decodeURIComponent(path)
    override suspend fun getTitle(): String = if (path == "" || path == "/") "Tasks" else getNameFromPath(decodeURIComponent(path))

    val tasks by lazy {
        async {
            val path = UTF8.urlDecode(path)
            Client.taskManager.getItems(path)
        }
    }

    private val scroll = ScrollController(dom)
    val toolBar = Toolbar().appendTo(layout, grow = 0, shrink = 0).addSpace()
    val addJob = Button("Create Job").appendTo(toolBar)
    val addFolder = Button("Create Folder").appendTo(toolBar)

    init {
        val path = UTF8.urlDecode(path)

        addJob.click {
            async {
                val name = ElementNameDialog.show(null, true) ?: return@async
                val r: TaskItem = Client.taskManager.createJob(path, name) ?: return@async
                JobItem(r).appendTo(layout, grow = 0, shrink = 0)
            }
        }

        addFolder.click {
            async {
                val name = ElementNameDialog.show(null, true) ?: return@async
                val r: TaskItem = Client.taskManager.createFolder(path, name) ?: return@async
                FolderItem(r).appendTo(layout, grow = 0, shrink = 0)
            }
        }
    }

    override suspend fun next(page: String): Page? {
        if (page.startsWith("d")) {
            val page = page.removePrefix("d")
            val next = nextJobItem(path, page)
            return TasksPage(next)
        }
        if (page.startsWith("j")) {
            val page = page.removePrefix("j")
            val next = nextJobItem(pathDecode, page)
            return TaskPage(next, Client.taskManager.getJob(next)!!)
        }
        return super.next(page)
    }

    override suspend fun onInit() {
        super.onInit()
        toolBar.dom.style.apply {
            marginBottom = "20px"
        }

        tasks.await().forEach {

            val item = if (it.isTask) JobItem(it) else FolderItem(it)
            item.appendTo(layout, grow = 0, shrink = 0)
        }
    }

    private class JobItem(val job: TaskItem) : DivComponentWithLayout() {
        private val link = LinkPlace().appendTo(layout)
        private val title = Span(job.name).appendTo(link.layout)
        private val deleteBtn = Button("delete").appendTo(layout, grow = 0, shrink = 0)
        private val renameBtn = Button("rename").appendTo(layout, grow = 0, shrink = 0)

        init {
            dom.addClass(Styles.LIST_ITEM)
            title.dom.addClass(Styles.SIMPLE_TEXT)
            link.href = "${uiUrl}/tasks/${job.path.taskUrl()}"
            link.dom.onclick = {
                PageNavigator.goto(link.href)
                it.preventDefault()
            }

            deleteBtn.click {

            }
        }
    }

    private class FolderItem(val job: TaskItem) : LinkComponentWithLayout() {
        val link = Span(job.name).appendTo(layout)
        val deleteBtn = Button("delete").appendTo(layout, grow = 0, shrink = 0)

        init {
            dom.addClass(Styles.LIST_ITEM)
            link.dom.addClass(Styles.SIMPLE_TEXT)
            href = "${uiUrl}/tasks/${job.path.folderUrl()}"
            dom.onclick = {
                PageNavigator.goto(href)
                it.preventDefault()
            }
        }
    }
}

fun String.taskUrl(): String {
    val items = split('/')
    return items.mapIndexed { index, s ->
        if (index == items.lastIndex)
            "j${encodeURIComponent(s)}"
        else
            "d${encodeURIComponent(s)}"
    }.joinToString("/")
}

fun String.folderUrl() =
        split('/').map { "d${encodeURIComponent(it)}" }.joinToString("/")

class LinkPlace : LinkComponentWithLayout() {
    public override val layout
        get() = super.layout

    init {
        layout.alignItems = FlexLayout.AlignItems.Center
    }
}

 */