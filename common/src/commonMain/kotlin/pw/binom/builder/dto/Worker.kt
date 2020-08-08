package pw.binom.builder.dto

import kotlinx.serialization.Serializable

@Serializable
class Worker(
        val name: String,
        val tags: Set<String>,
        val id: String,
        val status: SlaveStatus?
) {
    @Serializable
    class SlaveStatus(val jobPath: String, val buildNumber: Int, val startBuildTime: Long)
}