package pw.binom.builder.web.ace

import org.w3c.dom.HTMLElement

@JsName("ace")
external object Ace {
    fun edit(id: String): Editor
    fun edit(id: HTMLElement): Editor
}