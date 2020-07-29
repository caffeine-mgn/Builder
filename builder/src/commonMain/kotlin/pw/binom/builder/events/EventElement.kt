package pw.binom.builder.events

import pw.binom.io.Closeable
import pw.binom.thread.Lock
import pw.binom.thread.synchronize

class EventElement {
    private val listeners = ArrayList<() -> Unit>()
    private val lock = Lock()

    fun on(listener: () -> Unit): Closeable {
        lock.synchronize {
            listeners += listener
        }
        return Closeable {
            lock.synchronize {
                listeners -= listener
            }
        }
    }

    fun dispatch() {
        lock.synchronize {
            listeners.toTypedArray().forEach {
                it()
            }
        }
    }
}