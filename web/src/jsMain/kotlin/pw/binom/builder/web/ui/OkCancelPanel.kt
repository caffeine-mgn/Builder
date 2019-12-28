package pw.binom.builder.web

import org.tlsys.css.CSS
import kotlin.dom.addClass

private val ElementItemsStyle = CSS.style {
    "*" {
        marginLeft = "5px"
//        paddingLeft = "30px"
//        paddingRight = "30px"
        width = "100px"
    }
}.name

class OkCancelPanel(ok: String = "OK", cancel: String = "Cancel") : DivComponentWithLayout() {
    val ok = Button(ok).appendTo(layout, grow = 0, shrink = 0)
    val cancel = Button(cancel).appendTo(layout, grow = 0, shrink = 0)

    init {
        dom.addClass(ElementItemsStyle)
        layout.justifyContent = FlexLayout.JustifyContent.Center
    }
}