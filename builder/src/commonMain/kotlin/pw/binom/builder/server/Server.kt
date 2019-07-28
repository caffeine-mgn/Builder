package pw.binom.builder.server

import pw.binom.Date
import pw.binom.PopResult
import pw.binom.Stack
import pw.binom.io.Closeable
import pw.binom.io.file.File
import pw.binom.io.httpServer.HttpServer
import pw.binom.io.socket.ConnectionManager
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class EventBus<T> : Closeable {

    private val result2 = PopResult<Continuation<T>>()
    private val waitList = Stack<Continuation<T>>()

    fun push(value: T) {
        while (true) {
            waitList.popFirst(result2)
            if (result2.isEmpty)
                break
            result2.value.resume(value)
        }
    }

    suspend fun pop(): T {
        return suspendCoroutine { v ->
            waitList.pushLast(v)
        }
    }

    override fun close() {
        while (true) {
            waitList.popFirst(result2)
            if (result2.isEmpty)
                break
            result2.value.resumeWithException(RuntimeException("Cancel"))
        }
    }

}

enum class Output {
    STDOUT,
    STDERR
}

class Status(val type: Type, val time: Long) {
    val date: Date
        get() = Date(time)

    enum class Type {
        PREPARE,
        PROCESS,
        FINISHED_OK,
        FINISHED_ERROR,
        CANCELED
    }
}

class AsyncStack<T> : Closeable {
    override fun close() {
        waitList.forEach {
            it.second.resumeWithException(RuntimeException("Cancel"))
        }
        waitList.clear()
    }

    private val waitList = ArrayList<Pair<(T) -> Boolean, Continuation<T>>>()
    private val values = ArrayList<T>()

    fun push(value: T) {
        waitList.forEachIndexed { index, pair ->
            if (pair.first(value)) {
                waitList.removeAt(index)
                pair.second.resume(value)
                return
            }
        }

        values += value
    }

    suspend fun pop(filter: (T) -> Boolean): T {
        val v = values.asSequence().filter(filter).firstOrNull()
        if (v != null) {
            values.remove(v)
            return v
        }

        return suspendCoroutine { v ->
            waitList.add(filter to v)
        }
    }
}

class Server(val jobsPath: File, val bind: List<Pair<String, Int>>) {
    val taskManager = TaskManager(jobsPath)
    val executionControl = ExecutionControl(taskManager)
    val executeScheduler = ExecuteScheduler()
    fun start() {
        val manager = ConnectionManager()
        val server = HttpServer(
                manager = manager,
                handler = RootHandler(
                        taskManager = taskManager,
                        executeScheduler = executeScheduler,
                        executionControl = executionControl
                )
        )
        bind.forEach {
            server.bindHTTP(host = it.first, port = it.second)
        }

        while (true) {
            executeScheduler.update()
            manager.update(1000)
        }
    }
}