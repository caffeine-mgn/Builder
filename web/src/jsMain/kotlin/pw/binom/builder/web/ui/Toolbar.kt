package pw.binom.builder.web

import org.tlsys.css.CSS
import org.w3c.dom.HTMLElement
import kotlin.dom.addClass

private val PanelStyle = CSS.style {
    background = "linear-gradient(rgba(15,15,15,1) 0%, rgba(31,31,31,1) 100%)"
    height = "33px"
    borderBottom = "1px solid #000"
    padding = "6px 10px"
}.name

private val ItemStyle = CSS.style {
    marginLeft = "5px"
    marginRight = "5px"
}.name

class Toolbar : DivComponentWithLayout() {
    init {
        dom.addClass(PanelStyle)
        layout.direction = FlexLayout.Direction.Row
    }

    fun <T : HTMLElement, C : Component<T>> add(component: C): C {
        component.appendTo(layout, grow = 0, shrink = 0)
        component.dom.addClass(ItemStyle)
        return component
    }

    fun addSpace(): Toolbar {
        EmptySpace().appendTo(layout, grow = 1, shrink = 1)
        return this
    }
}

private class EmptySpace : DivComponent()

fun <T : HTMLElement, C : Component<T>> C.appendTo(toolbar: Toolbar): C {
    toolbar.add(this)
    return this
}