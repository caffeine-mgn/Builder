package pw.binom.builder.web

import org.w3c.dom.HTMLSpanElement
import kotlin.browser.document

class Span(text: String = "") : AbstractComponent<HTMLSpanElement>(document.createElement("span").unsafeCast<HTMLSpanElement>()) {
    var text: String
        get() = dom.innerText
        set(value) {
            dom.innerText = value
        }

    init {
        this.text = text
    }
}