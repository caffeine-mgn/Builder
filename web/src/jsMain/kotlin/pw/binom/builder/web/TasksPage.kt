package pw.binom.builder.web

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