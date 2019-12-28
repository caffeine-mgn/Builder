package pw.binom.builder.web

import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import kotlin.browser.document

interface Component<T : HTMLElement> {
    val dom: T
    suspend fun onStart()
    suspend fun onStop()
}

var <T : HTMLElement> T.component: Component<T>?
    get() = this.asDynamic().CONTROL
    set(value) {
        this.asDynamic().CONTROL = value
    }

abstract class AbstractComponent<T : HTMLElement>(override val dom: T) : Component<T> {
    init {
        dom.component = this
    }

    private var started = false

    override suspend fun onStart() {
        if (!started) {
            started = true
            onInit()
        }
    }

    override suspend fun onStop() {
    }

    protected open suspend fun onInit() {

    }
}

open class DivComponent(dom: HTMLDivElement = document.createDiv()) : AbstractComponent<HTMLDivElement>(dom)
abstract class DivComponentWithLayout(
        dom: HTMLDivElement = document.createDiv(),
        alignItems: FlexLayout.AlignItems = FlexLayout.AlignItems.Stretch,
        direction: FlexLayout.Direction = FlexLayout.Direction.Row) : DivComponent(dom) {
    protected open val layout = FlexLayout(dom, alignItems = alignItems, direction = direction)

    override suspend fun onStart() {
        super.onStart()
        layout.onStart()
    }

    override suspend fun onStop() {
        layout.onStop()
        super.onStop()
    }
}

open class DivLayout(dom: HTMLDivElement = document.createDiv(),
                     alignItems: FlexLayout.AlignItems = FlexLayout.AlignItems.Stretch,
                     direction: FlexLayout.Direction = FlexLayout.Direction.Row) : DivComponentWithLayout(dom = dom, alignItems = alignItems, direction = direction) {
    public override val layout: FlexLayout<HTMLDivElement>
        get() = super.layout
}