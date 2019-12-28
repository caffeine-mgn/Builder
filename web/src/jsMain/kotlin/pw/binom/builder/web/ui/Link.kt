package pw.binom.builder.web

import org.w3c.dom.HTMLAnchorElement
import kotlin.browser.document

open class LinkComponentWithLayout : AbstractComponent<HTMLAnchorElement>(document.createLink()) {
    open protected val layout = FlexLayout(dom)

    var href
        get() = dom.href
        set(value) {
            dom.href = value
        }
}

class Link(text: String = "") : AbstractComponent<HTMLAnchorElement>(document.createLink()) {
    var text
        get() = dom.innerText
        set(value) {
            dom.innerText = value
        }

    var href
        get() = dom.href
        set(value) {
            dom.href = value
        }

    init {
        this.text = text
    }
}

