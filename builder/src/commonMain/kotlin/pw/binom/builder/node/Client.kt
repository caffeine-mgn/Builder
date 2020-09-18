package pw.binom.builder.node

import pw.binom.URL
import pw.binom.UUID
import pw.binom.builder.master.services.SlaveService

interface Client {
    var state: SlaveService.SlaveStatus?
    val tags: List<String>
    val name: String
    val slaveId: UUID
    val serverUrl: URL
    var ttl: Long
    fun startTask(cmd: String, env: Map<String, String>, path: String, buildNumber: Int)
}