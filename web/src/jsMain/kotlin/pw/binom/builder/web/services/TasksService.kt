package pw.binom.builder.web.services

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.list
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import pw.binom.builder.dto.Entity
import pw.binom.builder.web.Request1
import pw.binom.builder.web.UnauthorizedException
import pw.binom.builder.web.encodeUrl

object TasksService {
    suspend fun getByPath(path: String) =
            try {
                Json.decodeFromString(Entity.serializer(), Request1.get("/api/tasks/${path.encodeUrl()}"))
            } catch (e: UnauthorizedException) {
                null
            }

    suspend fun createDirection(path: String) =
            Json.decodeFromString(Entity.Direction.serializer(), Request1.post("/api/tasks/${path.encodeUrl()}?dir"))

    suspend fun createJob(path: String, config: Entity.JobConfig) =
            Json.decodeFromString(Entity.Job.serializer(), Request1.post("/api/tasks/${path.encodeUrl()}", Json.encodeToString(Entity.JobConfig.serializer(), config)))

    suspend fun delete(path: String) {
        Request1.delete("/api/tasks/${path.encodeUrl()}")
    }

    suspend fun update(path: String, config: Entity.JobConfig) {
        Request1.put("/api/tasks/${path.encodeUrl()}", Json.encodeToString(Entity.JobConfig.serializer(), config))
    }

    suspend fun getList(path: String) =
            try {
                Json.decodeFromString(ListSerializer(Entity.serializer()), Request1.get("/api/tasks/${path.encodeUrl()}?list=true"))
            } catch (e: UnauthorizedException) {
                null
            }
}