package pw.binom.builder

import pw.binom.Stack
import pw.binom.io.Closeable
import pw.binom.io.ClosedException
import pw.binom.thread.Lock
import pw.binom.thread.synchronize
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/*
class Topic<T:Any> : Closeable {

    private val waiters = Stack<Continuation<T>>()
    private val lock = Lock()

    private var _closed: Boolean = false
    val closed
        get() = _closed

    override fun close() {
        lock.synchronize {
            while (!waiters.isEmpty) {
                waiters.popLast().resumeWithException(ClosedException())
            }
            _closed = true
        }
    }

    /**
     * @throws ClosedException возникает когда топик закрыт
     */
    fun dispatch(value: T) {

        lock.synchronize {
            val list = ArrayList<Continuation<T>>(waiters.size)
            if (closed)
                throw ClosedException()
            while (!waiters.isEmpty) {
                list.add(waiters.popLast())
            }
            list.forEach {
                it.resume(value)
            }
        }
    }

    /**
     * @throws ClosedException возникает когда топик закрылся
     */
    suspend fun wait(): T {
        return suspendCoroutine {
            lock.synchronize {
                waiters.pushFirst(it)
            }
        }
    }
}
*/