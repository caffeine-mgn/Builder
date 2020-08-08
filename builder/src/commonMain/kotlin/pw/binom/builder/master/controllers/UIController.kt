package pw.binom.builder.master.controllers

import pw.binom.ByteBuffer
import pw.binom.asUTF8String
import pw.binom.base64.Base64DecodeAppendable
import pw.binom.builder.Resources
import pw.binom.flux.Action
import pw.binom.flux.RootRouter
import pw.binom.flux.get
import pw.binom.io.*
import pw.binom.io.http.Headers
import pw.binom.strong.Strong

class UIController(val strong: Strong) : Strong.InitializingBean {
    private val rootRouter by strong.service<RootRouter>()

    private fun getResource(resource: Array<String>): ByteBuffer {
        val data = ByteArrayOutput()
        val decoder = Base64DecodeAppendable(data)
        resource.forEach {
            decoder.append(it)
        }
        data.trimToSize()
        data.clear()
        return data.data
    }

    val kotlin_js by lazy {
        getResource(Resources.kotlin_js)
    }

    val core_js by lazy {
        getResource(Resources.core_js)
    }

    val ace_js by lazy {
        getResource(Resources.ace_js)
    }

    val mode_sh_js by lazy {
        getResource(Resources.mode_sh_js)
    }

    val web_js by lazy {
        getResource(Resources.web_js)
    }

    val common_js by lazy {
        getResource(Resources.common_js)
    }

    val css_js by lazy {
        getResource(Resources.css_js)
    }

    val kotlinx_serialization_js by lazy {
        getResource(Resources.kotlinx_serialization_js)
    }

    val light_background_jpg by lazy {
        getResource(Resources.light_background_jpg)
    }

    private val index by lazy {
        val data = ByteArrayOutput()
        val out = data.utf8Appendable()
        out.append("<html><script>var serverUrl='http://127.0.0.1:8080/").append("';</script>")
                .append("<body></body>")
                .append("<script>window.theme='light';</script>")
                .append("<script src=\"/web/kotlin.js\"></script>")
                .append("<script src=\"/web/kotlinx-serialization-kotlinx-serialization-runtime.js\"></script>")
                .append("<script src=\"/web/ace.js\"></script>")
                .append("<script src=\"/web/mode-sh.js\"></script>")
                .append("<script src=\"/web/core.js\"></script>")
                .append("<script src=\"/web/common.js\"></script>")
                .append("<script src=\"/web/org.tlsys.css.js\"></script>")
                .append("<script src=\"/web/web.js\"></script>")
                .append("<link href=\"https://fonts.googleapis.com/css?family=Roboto&display=swap\" rel=\"stylesheet\">")
                .append("</html>")
        data.trimToSize()
        data.data
    }

    suspend fun Action.sendJpeg(data: ByteBuffer): Boolean {
        resp.status = 200
        resp.addHeader(Headers.CONTENT_TYPE, "image/jpeg")
        data.clear()
        resp.complete().write(data)
        return true
    }

    suspend fun Action.sendJs(data: ByteBuffer): Boolean {
        resp.status = 200
        resp.addHeader(Headers.CONTENT_TYPE, "application/javascript; charset=utf-8")
        data.clear()
        resp.complete().write(data)
        return true
    }

    override fun init() {
        rootRouter.get("/web/kotlin.js") {
            it.sendJs(kotlin_js)
        }
        rootRouter.get("/web/core.js") {
            it.sendJs(core_js)
        }
        rootRouter.get("/web/ace.js") {
            it.sendJs(ace_js)
        }
        rootRouter.get("/web/mode-sh.js") {
            it.sendJs(mode_sh_js)
        }

        rootRouter.get("/web/web.js") {
            it.sendJs(web_js)
        }

        rootRouter.get("/web/common.js") {
            it.sendJs(common_js)
        }

        rootRouter.get("/web/light-background.jpg") {
            it.sendJpeg(light_background_jpg)
        }
        rootRouter.get("/web/org.tlsys.css.js") {
            it.sendJs(css_js)
        }

        rootRouter.get("/web/kotlinx-serialization-kotlinx-serialization-runtime.js") {
            it.sendJs(kotlinx_serialization_js)
        }

        rootRouter.get("/*") {
            it.resp.status = 200
            it.resp.addHeader(Headers.CONTENT_TYPE, "text/html; charset=utf-8")
            index.clear()
            it.resp.complete().write(index)
            true
        }
    }
}