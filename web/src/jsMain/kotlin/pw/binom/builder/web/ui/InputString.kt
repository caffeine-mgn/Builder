package pw.binom.builder.web

import org.tlsys.css.CSS
import org.w3c.dom.HTMLInputElement
import kotlin.browser.document
import kotlin.dom.addClass

private val InputStyle = CSS.style {
    background = "#555"
    border = "1px solid #555"
    borderRadius = "0.25rem"
    color = "#fefefe"
    marginBottom = "0.125rem"
    padding = "0.625rem"
    border = "1px solid #555"
    fontFamily = Styles.DEFAULT_FONT
    transitionProperty = "border"
    transitionDuration = "${Styles.ANIMATION_SPEED}ms"

    ":focus" then {
        outlineStyle = "none"
        outlineColor = "transparent"
        outline = "0"
        boxShadow = "0 0 5px #898989"
        border = "1px solid #898989"
    }
}.name

class InputString(text: String = "") : Component<HTMLInputElement> {
    override suspend fun onStart() {
    }

    override suspend fun onStop() {
    }

    override val dom: HTMLInputElement = document.createElement("input").unsafeCast<HTMLInputElement>()

    var text: String
        get() = dom.value
        set(value) {
            dom.value = value
        }

    init {
        dom.type = "text"
        dom.addClass(InputStyle)
        this.text = text
    }
}