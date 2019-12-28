package pw.binom.builder.web

import org.w3c.dom.HTMLElement
import pw.binom.io.Closeable

enum class ScrollType(val css: String) {
    AUTO("auto"),
    VISIBLE("visible"),
    SCROLL("scroll"),
    NONE("hidden"),
    INHERIT("inherit")
}

open class ScrollController<T : HTMLElement>(
        val dom: T,
        scrollX: ScrollType = ScrollType.AUTO,
        scrollY: ScrollType = ScrollType.AUTO
) {

    fun moveToEndV() {
        dom.scrollTop = dom.scrollHeight.toDouble()
    }

    var scrollX: ScrollType
        get() = dom.scrollX
        set(it) {
            dom.scrollX = it
        }

    var scrollY: ScrollType
        get() = dom.scrollY
        set(it) {
            dom.scrollY = it
        }

    val visibleScrollY: Boolean
        get() {
            if (scrollY === ScrollType.VISIBLE)
                return true
            return dom.scrollHeight > dom.clientHeight
        }

    private val scrollListeners = ArrayList<() -> Unit>()

    fun addScrollListener(listener: () -> Unit): Closeable {
        scrollListeners += listener

        return object :Closeable {
            override fun close() {
                scrollListeners -= listener
            }
        }
    }

    val endScrollY: Boolean
        get() = dom.offsetHeight + dom.scrollTop >= dom.scrollHeight

    var x: Int
        get() {
            if (dom.scrollLeft == null || dom.scrollLeft == undefined)
                return 0
            return dom.scrollLeft.toInt()
        }
        set(it) {
            dom.scrollLeft = it.toDouble()
        }

    var y: Int
        get() {
            if (dom.scrollTop == null || dom.scrollTop == undefined)
                return 0
            return dom.scrollTop.toInt()
        }
        set(it) {
            dom.scrollTop = it.toDouble()
        }

    init {
        this.scrollX = scrollX
        this.scrollY = scrollX

        dom.addEventListener("scroll", {
            scrollListeners.toTypedArray().forEach {
                it()
            }
            for (g in scrollListeners.toTypedArray()) {
                g()
            }
        })
    }
}

var HTMLElement.scrollX: ScrollType
    get() {
        if (this.style.overflowX == "")
            return ScrollType.VISIBLE

        return ScrollType.valueOf(this.style.overflowX.toUpperCase())
    }
    set(it) {
        this.style.overflowX = it.css
        if (it == ScrollType.VISIBLE)
            this.style.overflowX = ""
    }

var HTMLElement.scrollY: ScrollType
    get() {
        if (this.style.overflowY == "")
            return ScrollType.VISIBLE

        return ScrollType.valueOf(this.style.overflowY.toUpperCase())
    }
    set(it) {
        this.style.overflowY = it.css
        if (it == ScrollType.VISIBLE)
            this.style.overflowY = ""
    }