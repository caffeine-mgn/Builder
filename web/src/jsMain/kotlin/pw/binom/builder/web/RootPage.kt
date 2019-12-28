package pw.binom.builder.web

object RootPage : Page() {
    override suspend fun getTitle(): String = "/"

    override suspend fun next(page: String): Page? =
            when (page) {
                "tasks" -> TasksPage("")
                "nodes" -> NodesPage()
                "" -> ForwardPage("$uiUrl/tasks")
                else -> super.next(page)
            }
}

class ForwardPage(val path: String) : Page() {
    override suspend fun getTitle(): String = path

    override suspend fun onStart() {
        super.onStart()
        PageNavigator.goto(path)
    }
}