package pw.binom.builder

import pw.binom.async
import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicReference
import pw.binom.io.socket.ConnectionManager
import pw.binom.thread.Thread

fun <T> ConnectionManager.sync(func: suspend () -> T): T {
    val done = AtomicBoolean(false)
    val result = AtomicReference<T>(null as T)
    async {
        try {
            println("try to call!..")
            result.value = func()
        } finally {
            done.value = true
        }
    }

    while (!done.value && !Thread.currentThread.isInterrupted) {
        println("execute....")
        update(1000)
    }
    return result.value
}