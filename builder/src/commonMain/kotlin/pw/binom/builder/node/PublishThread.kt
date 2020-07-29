package pw.binom.builder.node

import pw.binom.SynchronizedQueue
import pw.binom.async
import pw.binom.atomic.AtomicInt
import pw.binom.builder.common.Action
import pw.binom.io.http.websocket.MessageType
import pw.binom.io.use
import pw.binom.io.utf8Appendable
import pw.binom.printStacktrace
import pw.binom.thread.InterruptedException
import pw.binom.thread.Thread
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class PublishThread(val client: ClientThread) : Thread() {
    private val queue = SynchronizedQueue<Action>()
    private val lock = AtomicInt(0)
    fun publish(action: Action) {
        queue.push(action)
    }

    @OptIn(ExperimentalTime::class)
    override fun run() {
        while (!isInterrupted) {
            try {
                if (!lock.compareAndSet(0, 1)) {
                    sleep(100)
                    if (isInterrupted)
                        break
                    continue
                }
                val connection = client.currentConnection
                if (connection == null) {
                    sleep(1000)
                    lock.value = 0
                    continue
                }
                val action = queue.pop()
                async {
                    try {
                        val sendTime = measureTime {
                            println("Sending $action")
                            connection.write(MessageType.TEXT).utf8Appendable().use {
                                it.append(action.toJson())
                            }
                        }
                        println("Send $action. Time: $sendTime")
                    } catch (e: Throwable) {
                        e.printStacktrace()
                    } finally {
                        lock.value = 0
                    }
                }
            } catch (e: Throwable) {
                if (e is InterruptedException) {
                    break
                }
                e.printStacktrace()
            }
        }
    }
}