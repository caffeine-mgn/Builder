package pw.binom.builder.remote
/*
import pw.binom.io.Closeable
import pw.binom.json.JsonNode
import pw.binom.json.JsonObject
import pw.binom.json.string
import pw.binom.krpc.Struct
import pw.binom.rpc.JsonRpc

abstract class AbstractClient {

    protected abstract suspend fun call(service: String, args: JsonObject): JsonObject
    abstract fun tail(process: JobProcess, func: (String?) -> Unit): Closeable
    abstract fun events(func: (Struct?) -> Unit): Closeable
    val processService = ProcessServiceRemoteAsync(JsonRpc.implementAsync(DTO_LIST) {
        call("process", it)
    })

    val taskManager = TaskManagerServiceRemoteAsync(JsonRpc.implementAsync(DTO_LIST) {
        call("tasks", it)
    })

    val nodesService = NodesServiceRemoteAsync(JsonRpc.implementAsync(DTO_LIST) {
        call("nodes", it)
    })
}
*/