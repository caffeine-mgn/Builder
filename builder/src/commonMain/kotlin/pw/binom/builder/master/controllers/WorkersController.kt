package pw.binom.builder.master.controllers

import pw.binom.builder.dto.Worker
import pw.binom.builder.master.SlaveService
import pw.binom.flux.RootRouter
import pw.binom.flux.get
import pw.binom.strong.Strong

fun SlaveService.Slave.toDTO() =
        Worker(
                name = name,
                id = id.toString(),
                tags = tags,
                status = status?.let {
                    Worker.SlaveStatus(
                            jobPath = it.jobPath,
                            buildNumber = it.buildNumber,
                            startBuildTime = this.statusChangeTime
                    )
                }
        )

class WorkersController(strong: Strong) : Strong.InitializingBean {
    private val flux by strong.service<RootRouter>()
    private val slaveService by strong.service<SlaveService>()
    override fun init() {
        flux.get("/api/workers") {
            it.resp.jsonList(
                    slaveService.slaves.values.map { it.toDTO() }
            )
            true
        }
    }

}