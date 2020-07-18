package pw.binom.builder.web
/*
import pw.binom.Base64DecodeAppendable
import pw.binom.URL
import pw.binom.builder.Resources
import pw.binom.builder.server.PathHandler
import pw.binom.builder.server.equal
import pw.binom.builder.server.method
import pw.binom.builder.server.plus
import pw.binom.io.ByteArrayOutputStream
import pw.binom.io.http.Headers
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse
import pw.binom.io.utf8Appendable

class WebUiHandler(val url: URL) : PathHandler() {

    val kotlin_js by lazy {
        val data = ByteArrayOutputStream()
        val decoder = Base64DecodeAppendable(data)
        Resources.kotlin_js.forEach {
            decoder.append(it)
        }
        data.toByteArray()
    }

    val json_js by lazy {
        val data = ByteArrayOutputStream()
        val decoder = Base64DecodeAppendable(data)
        Resources.json_js.forEach {
            decoder.append(it)
        }
        data.toByteArray()
    }

    val common_js by lazy {
        val data = ByteArrayOutputStream()
        val decoder = Base64DecodeAppendable(data)
        Resources.common_js.forEach {
            decoder.append(it)
        }
        data.toByteArray()
    }

    val rpcJson_js by lazy {
        val data = ByteArrayOutputStream()
        val decoder = Base64DecodeAppendable(data)
        Resources.rpcJson_js.forEach {
            decoder.append(it)
        }
        data.toByteArray()
    }

    val mode_sh_js by lazy {
        val data = ByteArrayOutputStream()
        val decoder = Base64DecodeAppendable(data)
        Resources.mode_sh_js.forEach {
            decoder.append(it)
        }
        data.toByteArray()
    }

    val rpc_js by lazy {
        val data = ByteArrayOutputStream()
        val decoder = Base64DecodeAppendable(data)
        Resources.rpc_js.forEach {
            decoder.append(it)
        }
        data.toByteArray()
    }

    val core_js by lazy {
        val data = ByteArrayOutputStream()
        val decoder = Base64DecodeAppendable(data)
        Resources.core_js.forEach {
            decoder.append(it)
        }
        data.toByteArray()
    }

    val css_js by lazy {
        val data = ByteArrayOutputStream()
        val decoder = Base64DecodeAppendable(data)
        Resources.css_js.forEach {
            decoder.append(it)
        }
        data.toByteArray()
    }

    val ace_js by lazy {
        val data = ByteArrayOutputStream()
        val decoder = Base64DecodeAppendable(data)
        Resources.ace_js.forEach {
            decoder.append(it)
        }
        data.toByteArray()
    }

    val tomorrow_night_js by lazy {
        val data = ByteArrayOutputStream()
        val decoder = Base64DecodeAppendable(data)
        Resources.tomorrow_night_js.forEach {
            decoder.append(it)
        }
        data.toByteArray()
    }

    val web_js by lazy {
        val data = ByteArrayOutputStream()
        val decoder = Base64DecodeAppendable(data)
        Resources.web_js.forEach {
            decoder.append(it)
        }
        data.toByteArray()
    }

    init {
        filter(method("GET")) { r, q ->
            q.status = 200
            val out = q.output.utf8Appendable()
            q.addHeader(Headers.CONTENT_TYPE, "text/html; charset=utf-8")
            out.append("<html><script>var serverUrl='").append(url.toString()).append("';</script>")
                    .append("<body></body>")
                    .append("<script src=\"${url.toString().removeSuffix("/")}/web/kotlin.js\"></script>")
                    .append("<script src=\"${url.toString().removeSuffix("/")}/web/ace.js\"></script>")
                    .append("<script src=\"${url.toString().removeSuffix("/")}/web/mode-sh.js\"></script>")
                    .append("<script src=\"${url.toString().removeSuffix("/")}/web/core.js\"></script>")
                    .append("<script src=\"${url.toString().removeSuffix("/")}/web/json.js\"></script>")
                    .append("<script src=\"${url.toString().removeSuffix("/")}/web/rpc.js\"></script>")
                    .append("<script src=\"${url.toString().removeSuffix("/")}/web/rpcJson.js\"></script>")
                    .append("<script src=\"${url.toString().removeSuffix("/")}/web/common.js\"></script>")
                    .append("<script src=\"${url.toString().removeSuffix("/")}/web/org.tlsys.css.js\"></script>")
                    .append("<script src=\"${url.toString().removeSuffix("/")}/web/web.js\"></script>")
                    .append("<link href=\"https://fonts.googleapis.com/css?family=Roboto&display=swap\" rel=\"stylesheet\">")
                    .append("</html>")
            q.output.flush()
        }

        filter(method("GET") + equal("web/kotlin.js")) { r, q ->
            q.status = 200
            q.addHeader(Headers.CONTENT_TYPE, "application/javascript; charset=utf-8")
            q.output.write(kotlin_js)
            q.output.flush()
        }

        filter(method("GET") + equal("web/ace.js")) { r, q ->
            q.status = 200
            q.addHeader(Headers.CONTENT_TYPE, "application/javascript; charset=utf-8")
            q.output.write(ace_js)
            q.output.flush()
        }

        filter(method("GET") + equal("web/mode-sh.js")) { r, q ->
            q.status = 200
            q.addHeader(Headers.CONTENT_TYPE, "application/javascript; charset=utf-8")
            q.output.write(mode_sh_js)
            q.output.flush()
        }

        filter(method("GET") + equal("web/web.js")) { r, q ->
            q.status = 200
            q.addHeader(Headers.CONTENT_TYPE, "application/javascript; charset=utf-8")
            q.output.write(web_js)
            q.output.flush()
        }


        filter(method("GET") + equal("web/theme-tomorrow_night.js")) { r, q ->
            q.status = 200
            q.addHeader(Headers.CONTENT_TYPE, "application/javascript; charset=utf-8")
            q.output.write(tomorrow_night_js)
            q.output.flush()
        }
        filter(method("GET") + equal("web/org.tlsys.css.js")) { r, q ->
            q.status = 200
            q.addHeader(Headers.CONTENT_TYPE, "application/javascript; charset=utf-8")
            q.output.write(css_js)
            q.output.flush()
        }

        filter(method("GET") + equal("web/json.js")) { r, q ->
            q.status = 200
            q.addHeader(Headers.CONTENT_TYPE, "application/javascript; charset=utf-8")
            q.output.write(json_js)
            q.output.flush()
        }
        filter(method("GET") + equal("web/rpcJson.js")) { r, q ->
            q.status = 200
            q.addHeader(Headers.CONTENT_TYPE, "application/javascript; charset=utf-8")
            q.output.write(rpcJson_js)
            q.output.flush()
        }

        filter(method("GET") + equal("web/common.js")) { r, q ->
            q.status = 200
            q.addHeader(Headers.CONTENT_TYPE, "application/javascript; charset=utf-8")
            q.output.write(common_js)
            q.output.flush()
        }
        filter(method("GET") + equal("web/rpc.js")) { r, q ->
            q.status = 200
            q.addHeader(Headers.CONTENT_TYPE, "application/javascript; charset=utf-8")
            q.output.write(rpc_js)
            q.output.flush()
        }

        filter(method("GET") + equal("web/core.js")) { r, q ->
            q.status = 200
            q.addHeader(Headers.CONTENT_TYPE, "application/javascript; charset=utf-8")
            q.output.write(core_js)
            q.output.flush()
        }
    }

    override suspend fun request(req: HttpRequest, resp: HttpResponse) {
        super.request(req, resp)
    }
}

 */