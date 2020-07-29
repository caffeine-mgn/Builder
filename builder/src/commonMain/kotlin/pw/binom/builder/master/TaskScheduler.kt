package pw.binom.builder.master

import pw.binom.async
import pw.binom.builder.Event
import pw.binom.builder.common.Action
import pw.binom.builder.master.taskStorage.TaskStorage
import pw.binom.builder.master.taskStorage.findEntity
import pw.binom.printStacktrace
import pw.binom.strong.EventSystem
import pw.binom.strong.Strong
import pw.binom.thread.Thread

class TaskScheduler(strong: Strong) : Strong.InitializingBean {

    private data class ScheduledTask(val config: TaskStorage.JobConfig, val path: String, val buildNumber: Int)

    private val taskStorage by strong.service(TaskStorage::class)
    private val slaveService by strong.service<SlaveService>()
    private val eventSystem by strong.service<EventSystem>()
//    private val slaveFreeTopic by strong.service<Topic<SlaveFreeEvent>>(SLAVE_FREE_TOPIC)

    private val scheduledTasks = ArrayList<ScheduledTask>()

    class TaskNotFoundException(val path: String) : RuntimeException() {
        override val message: String?
            get() = path
    }

    fun submitTask(path: String, buildNumber: Int): Int? {
        val entity = taskStorage.findEntity(path)
        if (entity == null) {
            println("Task not found! [$path]")
            throw TaskNotFoundException(path)
        }

        if (entity !is TaskStorage.Job) {
            println("Task is not job")
            return null
        }

        val slave = slaveService.findFreeSlave(
                inclide = entity.config.include,
                exclude = entity.config.exclude
        )

        if (slave == null) {
            println("Slave not found")
            scheduledTasks += ScheduledTask(
                    config = entity.config,
                    buildNumber = buildNumber,
                    path = path
            )
        } else {
            async {
                try {
                    println("Send execite to node... thread: ${Thread.currentThread.id}")
                    slave.execute(Action.ExecuteTask(
                            path = path,
                            config = entity.config,
                            buildNumber = buildNumber
                    ))
                    println("Sendded!")
                } catch (e: Throwable) {
                    e.printStacktrace()
                }
            }
        }

        return buildNumber
    }

    fun submitTask(path: String): Int? {
        val entity = taskStorage.findEntity(path) ?: return null
        if (entity !is TaskStorage.Job)
            return null
        val build = entity.createBuild()
        return submitTask(path, build.number)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun init() {

        eventSystem.listen<Event.NodeChangeStatus> { event ->
            val task = scheduledTasks.removeLastOrNull() ?: return@listen
            val slave = slaveService.findFreeSlave(
                    inclide = task.config.include,
                    exclude = task.config.exclude
            )
            if (slave == null) {
                scheduledTasks.add(task)
                return@listen
            }
//            val slave = slaveService.slaves[UUID.fromString(event.slaveId)] ?: return@listen
            async {
                slave.execute(
                        Action.ExecuteTask(
                                path = task.path,
                                config = task.config,
                                buildNumber = task.buildNumber
                        )
                )
            }
        }
//        async {
//            while (true) {
//                val event = slaveFreeTopic.wait()
//                val task = scheduledTasks.removeLastOrNull() ?: continue
//                event.slave.execute(
//                        Action.ExecuteTask(
//                                path = task.path,
//                                config = task.config,
//                                buildNumber = task.buildNumber
//                        )
//                )
//            }
//        }
    }
}