package pw.binom.builder.web.dto

import kotlin.js.Json

class NodeInfo(val id: String, val platform: Platform, val dataCenter: String) {
    companion object {
        fun read(node: Json) = NodeInfo(
                id = node["id"]!!.toString(),
                platform = node["platform"]!!.toString().let { Platform.valueOf(it) },
                dataCenter = node["dataCenter"]!!.toString()
        )
    }
}