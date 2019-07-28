package pw.binom.builder

import pw.binom.io.Closeable
import pw.binom.io.ClosedException
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class Topic<T> : Closeable {

    private val waiters = ArrayList<Continuation<T>>()

    private var _closed: Boolean = false
    val closed
        get() = _closed

    override fun close() {
        waiters.forEach {
            it.resumeWithException(ClosedException())
        }
        waiters.clear()
        _closed = true
    }

    /**
     * @throws ClosedException возникает когда топик закрыт
     */
    fun dispatch(value: T) {
        if (closed)
            throw ClosedException()
        waiters.forEach {
            it.resume(value)
        }
        waiters.clear()
    }

    /**
     * @throws ClosedException возникает когда топик закрылся
     */
    suspend fun wait(): T {
        return suspendCoroutine {
            waiters.add(it)
        }
    }
}