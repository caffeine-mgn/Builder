package pw.binom.builder.web

import pw.binom.builder.Event
import pw.binom.builder.dto.Worker
import pw.binom.builder.web.services.EventService
import pw.binom.builder.web.services.WorkerService
import pw.binom.io.Closeable

class WorkersPage : Page() {
    override suspend fun getTitle(): String = "Workers"
    private var listener: Closeable? = null
    private val workers = ListView<WorkerItem>().appendTo(layout)


    override suspend fun onStart() {
        super.onStart()

        WorkerService.getList()!!.forEach {
            workers.addLast(WorkerItem(it))
        }

        listener = EventService.addListener { event ->
            when (event) {
                is Event.AddNode -> {
                    workers.addLast(WorkerItem(Worker(
                            name = event.name,
                            tags = event.tags,
                            id = event.id,
                            status = null
                    )))
                }
                is Event.DeleteNode -> {
                    workers.asSequence().find { it.worker.id == event.id }?.let {
                        workers.remove(it)
                    }
                }
            }
        }
    }

    override suspend fun onStop() {
        listener?.close()
        listener = null
        super.onStop()
    }

    class WorkerItem(val worker: Worker) : DivComponentWithLayout(direction = FlexLayout.Direction.Row) {
        val span = Span(worker.name).appendTo(layout)
    }
}