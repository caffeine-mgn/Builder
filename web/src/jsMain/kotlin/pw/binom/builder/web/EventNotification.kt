package pw.binom.builder.web

import org.tlsys.css.CSS
import org.w3c.dom.HTMLDivElement
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.addClass

private val ItemStyle = CSS.style {
    width = "300px"
    backgroundColor = "black"
    margin = "18px 25px"
    padding = "10px"
    borderRadius = "5px"
    "span"{
        color = "#eaeaea"
        fontFamily = Styles.DEFAULT_FONT
        fontSize = "14px"
    }
    opacity = 0.5
    transitionDuration = "0.3s"
    transitionProperty = "opacity"
    hover {
        opacity = 1.0
    }
}.name

private class NotifyBody : DivComponentWithLayout() {
    public override val layout: FlexLayout<HTMLDivElement>
        get() = super.layout

    init {
        layout.direction = FlexLayout.Direction.Column
        dom.style.apply {
            position = "fixed"
            bottom = "0px"
            left = "0px"
            zIndex = "4"
        }
    }

    fun add(text: String) {
        NotifyItem(text).appendTo(layout)
    }
}

class NotifyItem(text: String) : DivComponentWithLayout() {
    private val text = Span(text).appendTo(layout)

    init {
        layout.alignItems = FlexLayout.AlignItems.Center
        dom.addClass(ItemStyle)
    }

    override suspend fun onStart() {
        super.onStart()
        window.setTimeout({
            dom.remove()
        }, 5000)
    }
}

object EventNotification {

    private val notifyBody = NotifyBody()

    fun add(text: String) = notifyBody.add(text)

    fun start() {
        document.body!!.appendChild(notifyBody.dom)
        async {
            notifyBody.onStart()
        }
    }
}