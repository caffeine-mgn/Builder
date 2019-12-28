package pw.binom.builder.web

import org.w3c.dom.HTMLAnchorElement
import kotlin.browser.document

private class NavigationButton(text: String, href: String) : AbstractComponent<HTMLAnchorElement>(document.createLink()) {
    init {
        dom.innerText = text
        dom.href = "${uiUrl}/$href"

        dom.onclick = {
            it.preventDefault()
            PageNavigator.goto(dom.href)
        }
    }
}

object NavigateMenu : DivComponentWithLayout(direction = FlexLayout.Direction.Column) {
    private val tasks = NavigationButton("Tasks", "tasks").appendTo(layout, grow = 0, shrink = 0)
    private val nodes = NavigationButton("Nodes", "nodes").appendTo(layout, grow = 0, shrink = 0)

    init {
        dom.style.apply {
            backgroundColor = "black"
        }
    }
}