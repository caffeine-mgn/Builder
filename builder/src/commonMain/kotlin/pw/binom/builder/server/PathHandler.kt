package pw.binom.builder.server

import pw.binom.io.UTF8
import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse
import pw.binom.io.httpServer.withContextURI

open class PathHandler : Handler {
    override suspend fun request(req: HttpRequest, resp: HttpResponse) {
        try {
            try {
                for (i in filters.size - 1 downTo 0) {
                    val item = filters[i]
                    val rr = item.first.request(req) ?: continue
                    item.second(rr, resp)
                    return
                }
            } catch (e: Throwable) {
                exceptionHander(e, req, resp)
            }

            resp.status = 404
        } catch (e: Throwable) {
            resp.status = 500
            throw e
        }
    }

    protected suspend open fun exceptionHander(exception: Throwable, req: HttpRequest, resp: HttpResponse) {
        resp.status = 500
    }

    fun filter(filter: PathFilter, handler: Handler) {
        filter(filter) { r, q -> handler.request(r, q) }
    }

    fun filter(filter: PathFilter, handler: suspend (req: HttpRequest, resp: HttpResponse) -> Unit) {
        filters.add(filter to handler)
    }

    private val filters = ArrayList<Pair<PathFilter, suspend (req: HttpRequest, resp: HttpResponse) -> Unit>>()
}

interface PathFilter {
    fun request(req: HttpRequest): HttpRequest?
}

val HttpRequest.contextUriWithoutParams: String
    get() {
        val p = contextUri.indexOf('?')
        return if (p == -1)
            contextUri
        else
            contextUri.substring(0, p)
    }

val HttpRequest.params: Map<String, String?>
    get() {
        val p = contextUri.indexOf('?')
        return if (p == -1)
            emptyMap()
        else
            contextUri.substring(p + 1)
                    .splitToSequence('&')
                    .map {
                        val items = it.split('=')
                        items[0] to items.getOrNull(1)?.let { UTF8.urlDecode(it) }
                    }.toMap()
    }

private fun Map<String, String?>.asParamsURI(): String {
    return if (isEmpty())
        ""
    else
        "?" + asSequence().map {
            if (it.value == null) it.key else "${it.key}=${UTF8.urlEncode(it.value!!)}"
        }.joinToString("&")
}

fun endsWith(uri: String) = object : PathFilter {
    override fun request(req: HttpRequest): HttpRequest? {
        if (!req.contextUriWithoutParams.endsWith(uri))
            return null
        return req.withContextURI(req.contextUriWithoutParams.removeSuffix(uri) + req.params.asParamsURI())
    }
}

fun equal(uri: String) = object : PathFilter {
    override fun request(req: HttpRequest): HttpRequest? {
        if (req.contextUriWithoutParams != uri)
            return null
        return req.withContextURI("" + req.params.asParamsURI())
    }
}

fun startsWith(uri: String) = object : PathFilter {
    override fun request(req: HttpRequest): HttpRequest? {
        if (!req.contextUri.startsWith(uri))
            return null
        return req.withContextURI(req.contextUri.removePrefix(uri))
    }
}

fun method(name: String) = object : PathFilter {
    override fun request(req: HttpRequest): HttpRequest? {
        if (req.method != name)
            return null
        return req
    }
}

operator fun PathFilter.plus(other: PathFilter) = object : PathFilter {
    override fun request(req: HttpRequest): HttpRequest? {
        val r = this@plus.request(req) ?: return null
        return other.request(r)
    }

}