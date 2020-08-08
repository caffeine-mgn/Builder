package pw.binom.builder.master.controllers

import kotlinx.serialization.json.Json
import pw.binom.builder.Event
import pw.binom.builder.dto.Entity
import pw.binom.builder.master.SlaveService
import pw.binom.builder.master.contextUriWithoutParams
import pw.binom.builder.master.params
import pw.binom.builder.master.services.TaskSchedulerService
import pw.binom.builder.master.taskStorage.EntityHolder
import pw.binom.builder.master.taskStorage.TaskStorage
import pw.binom.builder.master.taskStorage.findEntity
import pw.binom.flux.*
import pw.binom.io.readText
import pw.binom.io.utf8Reader
import pw.binom.strong.Strong

class TasksController(strong: Strong) : Strong.InitializingBean {

    private val flux by strong.service<RootRouter>()
    private val taskStorage by strong.service<TaskStorage>()
    private val taskSchedulerService by strong.service<TaskSchedulerService>()
    private val slaveService by strong.service<SlaveService>()

    override fun init() {
        flux.get("/api/tasks/*") {
            val path = it.req.contextUriWithoutParams.removePrefix("/api/tasks/")
            println("path: [$path]")
            val entity = if (path.isEmpty()) taskStorage else taskStorage.findEntity(path) ?: return@get false
            println("-->$entity")
            when (entity) {
                is TaskStorage.Job -> it.resp.json(entity.jobToDto())
                is TaskStorage.Direction -> {
                    val list = it.req.params["list"]?.let {
                        it.size == 1 && (it[0] == null || it[0] == "true" || it[0] == "1")
                    } ?: false
                    if (list) {
                        val entitiesList = entity.getEntityList() ?: return@get false
                        it.resp.jsonList(entitiesList.map { it.toDTO() })
                    } else {
                        it.resp.json(entity.directionToDto())
                    }
                }
                is TaskStorage -> {
                    val entitiesList = entity.getEntityList() ?: return@get false
                    it.resp.jsonList(entitiesList.map { it.toDTO() })
                }
                else -> return@get false
            }
            true
        }

        flux.delete("/api/tasks/*") {
            val path = it.req.contextUriWithoutParams.removePrefix("/api/tasks/")
            if (slaveService.isTaskHold(path)) {
                it.resp.status = 423
                it.resp.complete()
                return@delete true
            }
            val entity = taskStorage.findEntity(path) ?: return@delete false
            entity.delete()
            true
        }

        flux.put("/api/tasks/*") {
            val path = it.req.contextUriWithoutParams.removePrefix("/api/tasks/")
            val entity = taskStorage.findEntity(path) ?: return@put false
            if (entity !is TaskStorage.Job) {
                it.resp.status = 405
                return@put true
            }
            entity.update(Json.parse(Entity.JobConfig.serializer(), it.req.input.utf8Reader().readText()).toInternal())
            it.resp.status = 204
            it.resp.complete()
            true
        }

        flux.post("/api/tasks/*") {
            val path = it.req.contextUriWithoutParams.removePrefix("/api/tasks/")
            val i = path.lastIndexOf('/')
            val parent: EntityHolder? = if (i == -1)
                taskStorage
            else
                taskStorage.findEntity(path.substring(0, i))?.let { it as? EntityHolder }
            parent ?: return@post false

            val name = if (i == -1)
                path
            else
                path.substring(i + 1)
            val dir = it.req.params["dir"]?.let { it.size == 1 && (it[0] == null || it[0] == "true" || it[0] == "1") }
                    ?: false
            if (dir) {
                it.resp.json(parent.createDirection(name).toDTO())
                return@post true
            } else {
                val config = Json.parse(Entity.JobConfig.serializer(), it.req.input.utf8Reader().readText()).toInternal()
                val job = parent.createJob(
                        name = name,
                        config = config
                )
                it.resp.json(job.jobToDto())
                return@post true
            }
            true
        }
    }

    fun TaskStorage.Job.jobToDto() =
            Entity.Job(
                    path = path,
                    lastBuildTime = this.lastBuildTime,
                    config = config.toDto(),
                    builds =
                    (taskSchedulerService.findScheduled(path).map {
                        Entity.Job.Build(
                                worker = null,
                                status = Event.TaskChangeStatus.JobStatusType.PREPARE,
                                buildNumber = it
                        )
                    }
                            + slaveService.findWorkerOnWork(path).map {
                        Entity.Job.Build(
                                worker = it.toDTO(),
                                status = this.getBuild(it.status!!.buildNumber)!!.status.toDTO(),
                                buildNumber = it.status!!.buildNumber
                        )
                    }).toMutableList()
            )

    fun TaskStorage.Direction.directionToDto() =
            Entity.Direction(path)

    fun TaskStorage.Entity.toDTO() =
            when (this) {
                is TaskStorage.Job -> jobToDto()
                is TaskStorage.Direction -> directionToDto()
                else -> throw RuntimeException("Unknown Entity ${this::class.simpleName}")
            }
}

fun TaskStorage.JobConfig.toDto() =
        Entity.JobConfig(
                cmd = cmd,
                env = env,
                include = include,
                exclude = exclude
        )

fun Entity.JobConfig.toInternal() =
        TaskStorage.JobConfig(
                cmd = cmd,
                env = env,
                include = include,
                exclude = exclude
        )