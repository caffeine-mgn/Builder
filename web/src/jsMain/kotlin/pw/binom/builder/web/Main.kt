package pw.binom.builder.web

import org.tlsys.css.CSS
import org.w3c.xhr.XMLHttpRequest
import pw.binom.builder.remote.*
import pw.binom.io.Closeable
import kotlin.browser.document
import kotlin.browser.window
import kotlin.coroutines.*
import kotlin.js.Json
import kotlin.js.Promise


val serverUrl: String
    get() = js("serverUrl").unsafeCast<String>().removeSuffix("/")

val uiUrl: String
    get() = "$serverUrl"

class Response(val code: String, val body: String)

fun <T> (suspend () -> T).start() = Promise<T> { resolve, reject ->
    this.startCoroutine(object : Continuation<T> {
        override fun resumeWith(result: Result<T>) {
            if (result.isSuccess)
                result.onSuccess(resolve)
            if (result.isFailure)
                reject(result.exceptionOrNull()!!)
        }

        override val context = EmptyCoroutineContext
    })
}

fun <T> async(c: suspend () -> T): Promise<T> = c.start()

suspend fun <T> Promise<T>.await() = suspendCoroutine<T> { c ->
    then({ c.resume(it) }, { c.resumeWithException(it) })
}


object Request {

    fun tail(path: String, func: (String?) -> Unit): Closeable {
        val rr = XMLHttpRequest()
        rr.withCredentials = true
        var lastPosition = 0L

        var str = ""
        var canceled = false

        fun doit() {
            if (rr.responseText.length == lastPosition.toInt())
                return
            val txt = rr.responseText.substring(lastPosition.toInt())
            str += txt
            while (true) {
                val p = str.indexOf('\n')
                if (p == -1)
                    break
                val tt = str.substring(0, p)
                window.setTimeout({
                    func(tt)
                }, 0)
                str = str.substring(p + 1)
                if (str.isEmpty())
                    break
            }
            lastPosition = rr.responseText.length.toLong()
        }

        rr.onreadystatechange = {
            if (rr.readyState == XMLHttpRequest.DONE && !canceled) {
                doit()
                func(null)
            }
        }
        rr.onprogress = {
            if (!canceled)
                doit()
        }
        rr.open(method = "GET", url = "$serverUrl/${path.removePrefix("/")}", async = true)
        rr.send()

        return object : Closeable {
            override fun close() {
                canceled = true
                rr.abort()
            }
        }
    }

    fun get(path: String) = Promise<Response> { d, c ->
        val rr = XMLHttpRequest()
        rr.withCredentials = true

        rr.onreadystatechange = {
            if (rr.readyState == XMLHttpRequest.DONE) {
                d(Response(code = rr.status.asDynamic(), body = rr.responseText))
            }
        }
        rr.open(method = "GET", url = "$serverUrl/${path.removePrefix("/")}", async = true)
        rr.send()
    }

    fun post(path: String, body: Json? = null) = Promise<Response> { d, c ->
        val rr = XMLHttpRequest()
        rr.withCredentials = true

        rr.onreadystatechange = {
            if (rr.readyState == XMLHttpRequest.DONE) {
                d(Response(code = rr.status.asDynamic(), body = rr.responseText))
            }
        }
        rr.open(method = "POST", url = "$serverUrl/${path.removePrefix("/")}", async = true)
        if (body == null)
            rr.send()
        else {
            rr.setRequestHeader("Content-Type", "application/json")
            rr.send(JSON.stringify(body))
        }
    }
}

suspend fun fff(): Char? {
    if (window.location.href == "11")
        return null
    else
        return '1'
}

fun main() {
    CSS {
        "*"{
            minHeight = "0px"
            minWidth = "0px"
        }
        "body"{
            height = "100%"
            width = "100%"
            margin = "0px"
            backgroundColor = "#262626"
        }
    }
    val mainLayout = FlexLayout(document.body!!)
    NavigateMenu.appendTo(mainLayout,grow = 0,shrink = 0)

    val pageLayout=DivLayout(direction = FlexLayout.Direction.Column)
    pageLayout.appendTo(mainLayout)
//    val pageLayout = FlexLayout(document.body!!)
    BreadCrumbs.appendTo(pageLayout.layout, grow = 0, shrink = 0)

    HLine().appendTo(pageLayout.layout, grow = 0, shrink = 0)

    val pageViewDiv = document.createDiv()
    pageViewDiv.style.height = "100%"
    pageLayout.layout.add(pageViewDiv) {
        grow = 1
        shrink = 1
    }
    PageView.start(RootPage, pageViewDiv)
    PageNavigator.start(RootPage)
    console.info(BreadCrumbs.dom)
    EventNotification.start()
    EventBus.start()
    EventBus.wait {
        when (it) {
            is Event_AttachNode -> EventNotification.add("Node attached ${it.node.id}")
            is Event_DetachNode -> EventNotification.add("Node detached ${it.node.id}")
            is Event_TaskStatusChange -> when (it.statusEnum) {
                JobStatusType.PROCESS -> EventNotification.add("Start build Task ${it.process.asShort}")
                JobStatusType.FINISHED_ERROR -> EventNotification.add("Task ${it.process.asShort} finished with error")
                JobStatusType.FINISHED_OK -> EventNotification.add("Task ${it.process.asShort} finished")
                JobStatusType.CANCELED -> EventNotification.add("Task ${it.process.asShort} cancelled")
                JobStatusType.PREPARE -> EventNotification.add("Task ${it.process.asShort} is starting to prepare")
            }
        }
    }
}