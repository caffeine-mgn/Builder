package pw.binom.builder.master.controllers

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializerOrNull
import pw.binom.ByteBufferPool
import pw.binom.UUID
import pw.binom.base64.Base64
import pw.binom.builder.PasswordUtils
import pw.binom.builder.master.POOL_8KB
import pw.binom.builder.master.services.SessionService
import pw.binom.builder.master.services.UserService
import pw.binom.date.Date
import pw.binom.flux.RootRouter
import pw.binom.flux.get
import pw.binom.flux.post
import pw.binom.io.http.Headers
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse
import pw.binom.io.httpServer.multipart
import pw.binom.io.readText
import pw.binom.io.utf8Appendable
import pw.binom.io.utf8Reader
import pw.binom.skipAll
import pw.binom.strong.Strong

private const val SESSION_KEY = "SESSION"

class UserController(strong: Strong) : Strong.InitializingBean {
    private val flux by strong.service<RootRouter>()
    private val bufferPool by strong.service<ByteBufferPool>(POOL_8KB)
    private val userService by strong.service<UserService>()
    private val sessionService by strong.service<SessionService>()
    override fun init() {
        flux.get("/api/whoiam") {
            val user = it.req.session?.let {
                println("Session: $it")
                val s = sessionService.getSession(it)
                println("UserId: $s")
                s
            }
                    ?.let {
                        val user = userService.getUserById(it)
                        println("User: $user")
                        user
                    }
                    ?: run {
                        it.resp.status = 401
                        it.resp.complete()
                        return@get true
                    }
            it.resp.json(user.toDTO())
            true
        }
        flux.post("/api/login") {
            if (it.req.session != null) {
                it.resp.status = 400
                it.resp.complete()
            }
            val multipart = it.req.multipart(bufferPool) ?: return@post false
            var login: String? = null
            var password: String? = null
            val tmp = bufferPool.borrow()
            try {
                while (multipart.next()) {
                    when (multipart.formName) {
                        "login" -> login = multipart.utf8Reader().readText()
                        "password" -> password = PasswordUtils.encode(multipart.utf8Reader().readText())
                        else -> multipart.skipAll(tmp)
                    }
                }
            } finally {
                bufferPool.recycle(tmp)
            }
            login ?: run {
                it.resp.status = 400
                it.resp.complete()
                return@post true
            }
            password ?: run {
                it.resp.status = 400
                it.resp.complete()
                return@post true
            }
            val user = userService.checkLoginPassword(
                    login = login,
                    password = password
            ) ?: run {
                it.resp.status = 401
                it.resp.complete()
                return@post true
            }
            val exp = Date(Date.now + 1000 * 60 * 60 * 24 * 7)
            val session = sessionService.createSession(
                    userId = user.id,
                    expirationDate = exp
            )
            it.resp.addHeader(Headers.SET_COOKIE, "$SESSION_KEY=$session; Expires=${exp.calendar(0)}; HttpOnly")
            it.resp.json(user.toDTO())
            true
        }
        flux.post("/api/logout") {
            val session = it.req.session ?: run {
                it.resp.status = 401
                it.resp.complete()
                return@post true
            }
            sessionService.deleteSession(session)
            it.resp.addHeader(Headers.SET_COOKIE, "Set-Cookie: $SESSION_KEY=0; Expires=Wed, 21 Oct 2015 07:28:00 GMT")
            it.resp.complete()
            true
        }
    }
}

@OptIn(ImplicitReflectionSerializer::class)
inline suspend fun <reified T : Any> HttpResponse.jsonList(obj: List<T>) {
    status = 200
    addHeader(Headers.CONTENT_TYPE, "application/json; charset=utf-8")
    val serializer = T::class.serializerOrNull()
            ?: throw RuntimeException("Can't get serializer for ${T::class.simpleName}")
    complete()
            .utf8Appendable()
            .append(Json.stringify(serializer.list, obj))
}

@OptIn(ImplicitReflectionSerializer::class)
inline suspend fun <reified T : Any> HttpResponse.json(obj: T) {
    status = 200
    addHeader(Headers.CONTENT_TYPE, "application/json; charset=utf-8")
    val serializer = T::class.serializerOrNull()
            ?: throw RuntimeException("Can't get serializer for ${T::class.simpleName}")
    complete()
            .utf8Appendable()
            .append(Json.stringify(serializer, obj))
}

val HttpRequest.session: UUID?
    get() =
        this.headers[Headers.COOKIE]
                ?.asSequence()
                ?.flatMap { it.splitToSequence("; ") }
                ?.map {
                    val items = it.split("=", limit = 2)
                    items[0] to items[1]
                }?.filter {
                    it.first == SESSION_KEY
                }?.map { it.second }
                ?.singleOrNull()
                ?.let {
                    UUID.fromString(it)
                }