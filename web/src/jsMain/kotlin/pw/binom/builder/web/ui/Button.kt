package pw.binom.builder.web

import org.tlsys.css.CSS
import org.w3c.dom.HTMLButtonElement
import kotlin.browser.document
import kotlin.dom.addClass

private val Style = CSS.style {
    background = "linear-gradient(rgba(244,104,0,1) 0%, rgba(194,82,0,1) 100%)"
    border = "1px solid #f46800"
    color = "#fcfed5"
    fontSize = "14px"
    padding = "5px"
    borderRadius = "3px"
    fontFamily = Styles.DEFAULT_FONT

    ":focus" then {
        outline = "0"
    }

    hover {
        outlineStyle = "none"
        outlineColor = "transparent"
        background = "#f46800"
        cursor = "pointer"
    }
}.name

class Button(label: String = "") : AbstractComponent<HTMLButtonElement>(document.createElement("button").unsafeCast<HTMLButtonElement>()) {
    var label: String
        get() = dom.innerText
        set(value) {
            dom.innerText = value
        }

    fun click(func: () -> Unit): Button {
        dom.addEventListener("click", {
            try {
                func()
            } finally {
                it.preventDefault()
            }
        })
        return this
    }

    init {
        this.label = label
        dom.addClass(Style)

        dom.addEventListener("mousedown", { it.preventDefault() })
        dom.addEventListener("mouseup", { it.preventDefault() })
    }
}