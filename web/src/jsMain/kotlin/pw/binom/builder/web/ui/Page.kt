package pw.binom.builder.web

import org.w3c.dom.HTMLDivElement
import org.w3c.dom.events.KeyboardEvent
import pw.binom.io.Closeable
import kotlin.browser.document

abstract class Page : AbstractComponent<HTMLDivElement>(document.createDiv()) {
    protected val layout = FlexLayout(dom, direction = FlexLayout.Direction.Column)
    suspend open fun next(page: String): Page? = null
    abstract suspend fun getTitle(): String
    private val subscribers = ArrayList<() -> Closeable>()

    protected fun subscribe(func: () -> Closeable) {
        subscribers += func
    }

    private val keyDownEvents = ArrayList<(KeyboardEvent) -> Unit>()

    private val closeAfterStop = ArrayList<Closeable>()

    override suspend fun onStart() {
        super.onStart()
        subscribers.forEach {
            closeAfterStop += it()
        }
        keyDownEvents.forEach {
            closeAfterStop += dom.on("keydown") { e -> it(e as KeyboardEvent) }
        }
        layout.onStart()
    }

    override suspend fun onStop() {
        layout.onStop()
        closeAfterStop.forEach {
            it.close()
        }
        closeAfterStop.clear()
        super.onStop()
    }

    protected fun keyDownEvent(func: (KeyboardEvent) -> Unit) {
        keyDownEvents += func
    }

    init {
        dom.style.height = "100%"
    }
}