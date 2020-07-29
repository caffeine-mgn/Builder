package pw.binom

import pw.binom.io.Closeable
import pw.binom.thread.Lock
import pw.binom.thread.synchronize

class SynchronizedQueue<T> : Closeable {
    private val lock = Lock()
    private val condition = lock.newCondition()
    private val q = Stack<T>().asFiFoQueue()

    fun pop(): T =
            lock.synchronize {
                while (true) {
                    if (q.isEmpty)
                        condition.wait()
                    else
                        break
                }
                q.pop()
            }

    fun push(value: T) {
        lock.synchronize {
            q.push(value)
            condition.notify()
        }
    }

    override fun close() {
        lock.synchronize {
            condition.close()
            lock.close()
        }
    }
}