package pw.binom.builder.web

import org.tlsys.css.CSS
import org.tlsys.css.TreeSecretedCssClass
import org.w3c.xhr.FormData
import org.w3c.xhr.XMLHttpRequest
import pw.binom.builder.dto.User
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

class UnauthorizedException : RuntimeException()
class LockedException : RuntimeException()
class MethodNotAllowedException : RuntimeException()

object Request1 {

    suspend fun get(path: String) = suspendCoroutine<String> { c ->
        val rr = XMLHttpRequest()
        rr.withCredentials = true

        rr.onreadystatechange = {
            if (rr.readyState == XMLHttpRequest.DONE) {
                when (rr.status.toInt()) {
                    200 -> c.resume(rr.responseText)
                    204 -> c.resume("")
                    400 -> c.resumeWithException(RuntimeException("400 code"))
                    401 -> c.resumeWithException(UnauthorizedException())
                    405 -> c.resumeWithException(MethodNotAllowedException())
                    423 -> c.resumeWithException(LockedException())
                }
            }
        }
        rr.open(method = "GET", url = "$serverUrl/${path.removePrefix("/")}", async = true)
        rr.send()
    }

    suspend fun delete(path: String) = suspendCoroutine<String> { c ->
        val rr = XMLHttpRequest()
        rr.withCredentials = true

        rr.onreadystatechange = {
            if (rr.readyState == XMLHttpRequest.DONE) {
                when (rr.status.toInt()) {
                    200 -> c.resume(rr.responseText)
                    204 -> c.resume("")
                    400 -> c.resumeWithException(RuntimeException("400 code"))
                    401 -> c.resumeWithException(UnauthorizedException())
                    405 -> c.resumeWithException(MethodNotAllowedException())
                    423 -> c.resumeWithException(LockedException())
                }
            }
        }
        rr.open(method = "DELETE", url = "$serverUrl/${path.removePrefix("/")}", async = true)
        rr.send()
    }

    suspend fun put(url: String, formData: String): String =
            suspendCoroutine { c ->
                val rr = XMLHttpRequest()
                rr.withCredentials = true

                rr.onreadystatechange = {
                    if (rr.readyState == XMLHttpRequest.DONE) {
                        when (rr.status.toInt()) {
                            200 -> c.resume(rr.responseText)
                            204 -> c.resume("")
                            400 -> c.resumeWithException(RuntimeException("400 code"))
                            401 -> c.resumeWithException(UnauthorizedException())
                            405 -> c.resumeWithException(MethodNotAllowedException())
                            423 -> c.resumeWithException(LockedException())
                        }
                    }
                }
                rr.open(method = "POST", url = "$serverUrl/${url.removePrefix("/")}", async = true)
                rr.send(formData)
            }

    suspend fun post(url: String): String =
            suspendCoroutine { c ->
                val rr = XMLHttpRequest()
                rr.withCredentials = true

                rr.onreadystatechange = {
                    if (rr.readyState == XMLHttpRequest.DONE) {
                        when (rr.status.toInt()) {
                            200 -> c.resume(rr.responseText)
                            204 -> c.resume("")
                            400 -> c.resumeWithException(RuntimeException("400 code"))
                            401 -> c.resumeWithException(UnauthorizedException())
                            405 -> c.resumeWithException(MethodNotAllowedException())
                            423 -> c.resumeWithException(LockedException())
                        }
                    }
                }
                rr.open(method = "POST", url = "$serverUrl/${url.removePrefix("/")}", async = true)
                rr.send()
            }

    suspend fun post(url: String, formData: String): String =
            suspendCoroutine { c ->
                val rr = XMLHttpRequest()
                rr.withCredentials = true

                rr.onreadystatechange = {
                    if (rr.readyState == XMLHttpRequest.DONE) {
                        when (rr.status.toInt()) {
                            200 -> c.resume(rr.responseText)
                            204 -> c.resume("")
                            400 -> c.resumeWithException(RuntimeException("400 code"))
                            401 -> c.resumeWithException(UnauthorizedException())
                            405 -> c.resumeWithException(MethodNotAllowedException())
                            423 -> c.resumeWithException(LockedException())
                        }
                    }
                }
                rr.open(method = "POST", url = "$serverUrl/${url.removePrefix("/")}", async = true)
                rr.send(formData)
            }

    suspend fun post(url: String, formData: FormData): String =
            suspendCoroutine { c ->
                val rr = XMLHttpRequest()
                rr.withCredentials = true

                rr.onreadystatechange = {
                    if (rr.readyState == XMLHttpRequest.DONE) {
                        when (rr.status.toInt()) {
                            200 -> c.resume(rr.responseText)
                            204 -> c.resume("")
                            400 -> c.resumeWithException(RuntimeException("400 code"))
                            401 -> c.resumeWithException(UnauthorizedException())
                            405 -> c.resumeWithException(MethodNotAllowedException())
                            423 -> c.resumeWithException(LockedException())
                        }
                    }
                }
                rr.open(method = "POST", url = "$serverUrl/${url.removePrefix("/")}", async = true)
                rr.send(formData)
            }
}

operator fun String.compareTo(func: TreeSecretedCssClass.() -> Unit): Int = 0

fun initUi(user: User) {
    LoginPage.dom.remove()
    val mainLayout = FlexLayout(document.body!!)
    NavigateMenu.appendTo(mainLayout, grow = 0, shrink = 0)

    val pageLayout = DivLayout(direction = FlexLayout.Direction.Column)
    pageLayout.appendTo(mainLayout)

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
}

fun main() {
    CSS {
        "*"{
            minHeight = "0px"
            minWidth = "0px"
        }
        "body" then {
            height = "100%"
            width = "100%"
            margin = "0px"
            backgroundColor = "#262626"
        }
    }
    console.info("Hello!")

    val login = LoginPage
    document.body!!.append(login.dom)
    async {
        login.onStart()
    }
    return

//    val pageLayout = FlexLayout(document.body!!)


    console.info(BreadCrumbs.dom)
    EventNotification.start()
    /*
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
    */
}