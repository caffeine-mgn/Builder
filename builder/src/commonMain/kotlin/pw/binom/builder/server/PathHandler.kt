package pw.binom.builder.server

import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse
import pw.binom.io.httpServer.withContextURI

open class PathHandler : Handler {
    override suspend fun request(req: HttpRequest, resp: HttpResponse) {
        try {
            for (i in filters.size - 1 downTo 0) {
                val item = filters[i]
                val rr = item.first.request(req) ?: continue
                item.second(rr, resp)
                return
            }

            resp.status = 404
        } catch (e: Throwable) {
            resp.status = 500
            throw e
        }
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

fun endsWith(uri: String) = object : PathFilter {
    override fun request(req: HttpRequest): HttpRequest? {
        if (!req.contextUri.endsWith(uri))
            return null
        return req.withContextURI(req.contextUri.removeSuffix(uri))
    }
}

fun equal(uri: String) = object : PathFilter {
    override fun request(req: HttpRequest): HttpRequest? {
        if (req.contextUri != uri)
            return null
        return req.withContextURI("")
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