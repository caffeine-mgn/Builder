package pw.binom.builder.web

import org.w3c.dom.HTMLElement
import org.w3c.dom.Node

class ListView<T : Component<out HTMLElement>> : DivComponentWithLayout() {
    val scroll = ScrollController(dom)
    private var pos = 0

    override suspend fun onStart() {
        super.onStart()
        scroll.y = pos
    }

    override suspend fun onStop() {
        pos = scroll.y
        super.onStop()
    }

    init {
        layout.direction = FlexLayout.Direction.Column
    }

    fun addFirst(element: T) {
        layout.addFirst(element.dom) {
            grow = 0
            shrink = 0
        }
    }

    fun remove(element: T): Boolean {
        if (element.dom.parentElement !== dom)
            return false
        element.dom.remove()
        return true
    }

    fun asSequence() = dom.childNodes.asSequence()
            .filterIsInstance<HTMLElement>()
            .mapNotNull { it.component } as Sequence<T>

    fun status() {

    }

    fun addLast(element: T) {
        element.appendTo(layout, grow = 0, shrink = 0)
    }
}

