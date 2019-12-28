package pw.binom.builder.web

import org.tlsys.css.CSS
import org.w3c.dom.HTMLElement
import kotlin.browser.document
import kotlin.dom.addClass

private val Style = CSS.style {
    background = "#050505"
    height = "40px"
    borderBottom = "1px solid #000"
    padding = "0px 10px"
}.name

private val StyleItem = CSS.style {
    "a"{
        color = "#eaeaea"
        textDecoration = "none"
        fontFamily = Styles.DEFAULT_FONT
        fontSize = "18px"
    }

    position = "relative"
    paddingRight = "20px"

    ":not( :last-child) ::after" then {
        position = "absolute"
        content = "'>'"
        paddingLeft = "5px"
        fontFamily = Styles.DEFAULT_FONT
        fontSize = "18px"
    }
}.name

object BreadCrumbs : DivComponentWithLayout() {
    val scroll = ScrollController(dom)

    init {
        layout.direction = FlexLayout.Direction.Row
        layout.alignItems = FlexLayout.AlignItems.Center
        dom.addClass(Style)
    }

    private class Item(val target: Target) : DivComponent() {
        private val link = document.createLink()

        init {
            dom.addClass(StyleItem)
            dom.appendChild(link)
            link.text = target.title
            link.href = target.url
            link.onclick = {
                PageNavigator.goto(target.url)
                it.preventDefault()
            }
        }
    }

    fun setTarget(target: List<Target>) {
        val items = dom.childs.mapNotNull { (it as? HTMLElement)?.component as? Item }
        items.forEach {
            it.dom.remove()
        }

        target.forEach {
            Item(it).appendTo(layout, grow = 0, shrink = 0)
        }
    }

    class Target(val url: String, val title: String)
}