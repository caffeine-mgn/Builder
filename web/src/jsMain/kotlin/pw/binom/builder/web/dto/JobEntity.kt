package pw.binom.builder.web.dto

import kotlin.js.Json

sealed class JobEntity(val path: String) {
    class Job(path: String) : JobEntity(path)
    class Folder(path: String) : JobEntity(path)

    val name: String
        get() {
            val p = path.lastIndexOf('/')
            return if (p == -1)
                path
            else
                path.substring(p + 1)
        }

    companion object {
        fun read(node: Json) = when (node["type"]) {
            "job" -> Job(node["path"].asDynamic())
            "folder" -> Folder(node["path"].asDynamic())
            else -> TODO()
        }
    }
}