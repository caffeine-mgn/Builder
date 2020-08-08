package pw.binom.builder.master.controllers

import pw.binom.builder.master.contextUriWithoutParams
import pw.binom.builder.master.services.TaskSchedulerService
import pw.binom.builder.master.taskStorage.TaskStorage
import pw.binom.builder.master.taskStorage.findEntity
import pw.binom.flux.RootRouter
import pw.binom.flux.post
import pw.binom.io.http.Headers
import pw.binom.io.utf8Appendable
import pw.binom.strong.Strong

class TaskSchedulerController(strong: Strong) : Strong.InitializingBean {

    private val flux by strong.service<RootRouter>()
    private val taskStorage by strong.service<TaskStorage>()
    private val taskSchedulerService by strong.service<TaskSchedulerService>()

    override fun init() {
        flux.post("/api/scheduler/run/*") {
            val path = it.req.contextUriWithoutParams.removePrefix("/api/scheduler/run/")
            if (taskStorage.findEntity(path) !is TaskStorage.Job?) {
                return@post false
            }
            val buildNum = taskSchedulerService.submitTask(path)
            it.resp.status = 200
            it.resp.addHeader(Headers.CONTENT_TYPE, "text/plain; charset=utf-8")
            it.resp.complete().utf8Appendable().append(buildNum.toString())
            true
        }
    }

}