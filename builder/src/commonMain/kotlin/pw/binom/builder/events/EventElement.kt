package pw.binom.builder.events

import pw.binom.io.Closeable

class EventElement {
    private val listeners = ArrayList<() -> Unit>()

    fun on(listener: () -> Unit): Closeable {
        listeners += listener
        return Closeable {
            listeners -= listener
        }
    }

    fun dispatch() {
        listeners.toTypedArray().forEach {
            it()
        }
    }
}