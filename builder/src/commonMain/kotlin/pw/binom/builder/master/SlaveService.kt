package pw.binom.builder.master

import kotlinx.serialization.Serializable
import pw.binom.UUID
import pw.binom.builder.Event
import pw.binom.builder.common.Action
import pw.binom.date.Date
import pw.binom.io.Closeable
import pw.binom.io.http.websocket.MessageType
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.use
import pw.binom.io.utf8Appendable
import pw.binom.strong.EventSystem
import pw.binom.strong.Strong

class SlaveService(strong: Strong) : Closeable {

    private val eventSystem by strong.service<EventSystem>()

    @Serializable
    class SlaveStatus(val jobPath: String, val buildNumber: Int)

    inner class Slave(
            val name: String,
            val tags: Set<String>,
            val id: UUID,
            var lastTime: Date,
            val connection: WebSocketConnection
    ) {
        var status: SlaveStatus? = null
            set(value) {
                field = value
                val status = value?.let {
                    Event.SlaveStatus(
                            buildNumber = it.buildNumber,
                            jobPath = it.jobPath
                    )
                }
                this@SlaveService.eventSystem.dispatch(
                        Event.NodeChangeStatus(
                                slaveId = id.toString(),
                                status = status
                        )
                )
            }

        suspend fun execute(action: Action) {
            connection.write(MessageType.TEXT).utf8Appendable().use {
                it.append(action.toJson())
            }
        }
    }

    private val nodes = HashMap<UUID, Slave>()
    val slaves: Map<UUID, Slave>
        get() = nodes

    fun nodeExist(id: UUID): Boolean = nodes.containsKey(id)

    fun uptodate(id: UUID) {
        nodes[id]?.lastTime = Date(Date.now)
    }

    fun findFreeSlave(inclide: Set<String>, exclude: Set<String>) =
            nodes.values.asSequence()
                    .filter {
                        it.status == null
                    }
                    .filter {
                        exclude.isEmpty() || !it.tags.containsAny(exclude)
                    }
                    .filter {
                        inclide.isEmpty() || it.tags.containsAny(inclide)
                    }
                    .firstOrNull()

    fun delete(id: UUID) {
        nodes.remove(id) ?: return
        eventSystem.dispatch(Event.DeleteNode(id.toString()))
    }

    fun reg(connection: WebSocketConnection, name: String, id: UUID, tags: Set<String>): Slave {
        val slave = Slave(
                name = name,
                id = id,
                tags = tags,
                lastTime = Date(Date.now),
                connection = connection
        )
        nodes[id] = slave
        eventSystem.dispatch(Event.AddNode(id = id.toString(), tags = tags, name = name))
        return slave
    }

    override fun close() {
        TODO("Not yet implemented")
    }
}

fun <T> Collection<out T>.containsAny(collection: Collection<out T>): Boolean {
    if (isEmpty() || collection.isEmpty())
        return false

    forEach {
        if (it in collection)
            return true
    }
    return false
}