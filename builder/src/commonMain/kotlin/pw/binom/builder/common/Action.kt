package pw.binom.builder.common

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import pw.binom.builder.Event
import pw.binom.builder.master.SlaveService
import pw.binom.builder.master.controllers.toDTO
import pw.binom.builder.master.services.TaskSchedulerService
import pw.binom.builder.master.taskStorage.TaskStorage
import pw.binom.builder.master.taskStorage.findEntity
import pw.binom.builder.node.ClientThread
import pw.binom.strong.EventSystem
import pw.binom.strong.Strong

@OptIn(ImplicitReflectionSerializer::class)
private val dtoModule11 = SerializersModule {
    this.polymorphic(Action::class.serializer())
}

private val actionJsonSerialization = Json(JsonConfiguration.Stable.copy(
        classDiscriminator = "@class"
), dtoModule11)

@Serializable
sealed class Action {

    fun toJson(): String = actionJsonSerialization.stringify(serializer(), this)

    companion object {
        fun toAction(json: String): Action = actionJsonSerialization.parse(serializer(), json)
    }

    open suspend fun executeSlave(strong: Strong): Action? = null
    open suspend fun executeMaster(slave: SlaveService.Slave, strong: Strong): Action? = null

    @Serializable
    class SlaveChangeState(val status: SlaveService.SlaveStatus?) : Action() {
        override suspend fun executeMaster(slave: SlaveService.Slave, strong: Strong): Action? {
            slave.status = status
            return null
        }
    }

    @Serializable
    class TaskStatusChange(val path: String, val buildNumber: Int, val status: TaskStorage.JobStatusType) : Action() {
        override suspend fun executeMaster(slave: SlaveService.Slave, strong: Strong): Action? {
            val taskStorage by strong.service<TaskStorage>()
            val eventSystem by strong.service<EventSystem>()
            val entity = taskStorage.findEntity(path)
            if (entity == null) {
                println("Can't find task \"$path\"")
                return null
            }
            val job = entity as TaskStorage.Job
            val build = job.getBuild(buildNumber)!!
            build.status = this.status

            eventSystem.dispatch(
                    Event.TaskChangeStatus(
                            path = path,
                            worker = slave.toDTO(),
                            buildNumber = buildNumber,
                            status = status.toDTO()
                    )
            )
            return null
        }
    }

    @Serializable
    class NOP : Action()

    @Serializable
    class Ping : Action() {
        override suspend fun executeSlave(strong: Strong): Action? {
            println("Ping!")
            return Pong()
        }
    }

    @Serializable
    class Pong : Action() {
        override suspend fun executeMaster(slave: SlaveService.Slave, strong: Strong): Action? {
            println("Pong!")
            return null
        }
    }

    @Serializable
    class LogRecord(val text: String, val std: Boolean) : Action() {
        override suspend fun executeMaster(slave: SlaveService.Slave, strong: Strong): Action? {
            val status = slave.status
            if (status == null) {
                println("Node don't have any task")
                return null
            }
            val taskStorage by strong.service<TaskStorage>()
            val entity = taskStorage.findEntity(status.jobPath)
            if (entity == null) {
                println("Can't find task \"${status.jobPath}\"")
                return null
            }
            val job = entity as TaskStorage.Job
            val build = job.getBuild(status.buildNumber)!!
            if (std)
                build.addStdout(text)
            else
                build.addStderr(text)
            return null
        }
    }

//    @Serializable
//    class LogSubscribe : Action() {
//        override suspend fun executeSlave(strong: Strong): Action? {
//            val logOutput by strong.service<LogOutput>()
//            logOutput.subscribe()
//            return null
//        }
//    }

//    @Serializable
//    class LogUnsubscribe : Action() {
//        override suspend fun executeSlave(strong: Strong): Action? {
//            val logOutput by strong.service<LogOutput>()
//            logOutput.unsubscribe()
//            return null
//        }
//    }

    @Serializable
    class ExecuteTask(val path: String, val buildNumber: Int, val config: TaskStorage.JobConfig) : Action() {
        override suspend fun executeSlave(strong: Strong): Action? {
            println("Must execute $path:$buildNumber")
            val client by strong.service<ClientThread>()
            return if (!client.startBuild(
                            config = config,
                            buildNumber = buildNumber,
                            path = path
                    )) {
                println("Reject")
                RejectExecute(path, buildNumber)
            } else
                null
        }
    }

    @Serializable
    class RejectExecute(val path: String, val buildNumber: Int) : Action() {
        override suspend fun executeMaster(slave: SlaveService.Slave, strong: Strong): Action? {
            val taskScheduler by strong.service<TaskSchedulerService>()
            //taskScheduler.submitTask(path, buildNumber)
            return null
        }
    }

    @Serializable
    class CancelExecutingTask : Action() {
        override suspend fun executeSlave(strong: Strong): Action? {
            val client by strong.service<ClientThread>()
            client.cancelBuild()
            return null
        }
    }

    @Serializable
    class TaskPlanned(val buildNumber: Int) : Action()

    @Serializable
    class TaskNotFound : Action()
}

/**
 * Действия, который должен проделать сборочный узел
 */
/*
sealed class Action {
    object Cancel : Action()
    object ClearBuilds : Action()

    suspend fun write(appendable: AsyncAppendable) {
        jsonNode(appendable) {
            write(this)
        }
    }

    suspend fun write(ctx: ObjectCtx) {
        ctx.run {
            when (this@Action) {
                is Cancel -> string("type", "cancel")
                is ClearBuilds -> string("type", "clearBuilds")
            }
        }
    }

    companion object {
        suspend fun read(reader: AsyncReader): Action {
            val r = JsonDomReader()
            JsonReader(reader).accept(r)
            return read(r.node)
        }

        fun read(node: JsonNode): Action {
            val type = node.obj["type"]!!.string
            return when (type) {
                "cancel" -> Cancel
                "clearBuilds" -> ClearBuilds
                else -> TODO()
            }
        }
    }
}
*/