package pw.binom.builder.web
/*
import pw.binom.io.Closeable
import pw.binom.krpc.Struct
import kotlin.browser.window


object EventBus {
    private val listener: (Struct?) -> Unit = {event->
        if (event == null) {
            window.setTimeout({ start() }, 1000)
        } else {
            async {
                listeners.forEach { it(event) }
            }
        }
    }

    private val listeners = ArrayList<suspend (Struct) -> Unit>()

    fun start() {
        Client.events(listener)
    }

    fun wait(func: suspend (Struct) -> Unit): Closeable {
        listeners += func
        return object :Closeable {
            override fun close() {
                listeners -= func
            }
        }
    }
}*/
