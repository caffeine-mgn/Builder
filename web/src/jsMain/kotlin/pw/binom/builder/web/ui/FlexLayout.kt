package pw.binom.builder.web

import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.get
import kotlin.js.Promise

fun <V : Component<out HTMLElement>> V.appendTo(layout: FlexLayout<*>, grow: Int = 1, shrink: Int = 1, basis: Int? = null): V {
    layout.add(this.dom) {
        this.basis = basis
        this.grow = grow
        this.shrink = shrink
    }

    return this
}

class FlexLayout<T : HTMLElement>(parent: T, direction: Direction = Direction.Row, alignItems: AlignItems = AlignItems.Stretch) : Component<T> {
    override val dom: T = parent

    private var started = false

    override suspend fun onStart() {
        (0 until dom.childNodes.length)
                .mapNotNull { dom.childNodes[it] }
                .mapNotNull { it.asDynamic().CONTROL.unsafeCast<Component<*>?>() }
                .forEach { it.onStart() }
        started = true
    }

    override suspend fun onStop() {
        started = false
        (0 until dom.childNodes.length)
                .mapNotNull { dom.childNodes[it] }
                .mapNotNull { it.asDynamic().CONTROL.unsafeCast<Component<*>?>() }
                .forEach { it.onStop() }
    }

    private var _visible: Boolean = true
    var visible: Boolean
        get() = _visible
        set(it) {
            _visible = it
            refreshFlexAttr()
        }

    private fun refreshFlexAttr() {
        if (_visible) {
            if (_inline)
                dom.style.display = "inline-flex"
            else
                dom.style.display = "flex"
        } else {
            dom.style.display = "none"
        }
    }

    private var _inline: Boolean = false
    var inline: Boolean
        get() = _inline
        set(it) {
            _inline = it
            refreshFlexAttr()
        }

    private var _direction: Direction = Direction.Row
    private var _justifyContent: JustifyContent = JustifyContent.Start

    var justifyContent: JustifyContent
        get() = _justifyContent
        set(it) {
            _justifyContent = it
            dom.style.justifyContent = it.css
        }

    private var _alignItems: AlignItems = AlignItems.Stretch
    var alignItems: AlignItems
        get() = _alignItems
        set(it) {
            _alignItems = it
            dom.style.alignItems = it.css
        }

    private var _alignContent: AlignContent = AlignContent.Stretch
    var alignContent: AlignContent
        get() = _alignContent
        set(it) {
            _alignContent = it
            dom.style.alignContent = it.css
        }

    var direction: Direction
        get() = _direction
        set(it) {
            _direction = it
            dom.style.flexDirection = it.css
        }


    init {
        inline = inline
        this.direction = direction
        this.alignItems = alignItems
        alignContent = alignContent
        justifyContent = justifyContent
        refreshFlexAttr()
        parent.childNodes.length
        for (f in 0..parent.childNodes.length - 1) {
            val element = parent.childNodes.get(f)
            if (element is HTMLElement) {
                val item = FlexItem(element)
                js("element.FLEX_ITEM=item")
            }
        }
    }

    fun item(element: HTMLElement): FlexItem {
        if (element.parentNode !== dom)
            throw RuntimeException("Element not in layout")
        return js("element.FLEX_ITEM")
    }

    private fun prepareElement(element: HTMLElement, control: (FlexItem.() -> Unit)?) {
        val item = FlexItem(element)
        element.asDynamic().FLEX_ITEM = item
        if (control != null)
            item.control()
    }

    fun <T : HTMLElement> addFirst(element: T, control: (FlexItem.() -> Unit)? = null): T =
            if (dom.childElementCount == 0)
                add(element = element, control = control)
            else
                addBefore(element = element, before = dom.children.get(0) as HTMLElement, control = control)

    private fun callStart(element: Element) {
        val com = element.asDynamic().CONTROL ?: return
        if (com !is Component<*>)
            return
        async {
            com.onStart(Promise.Companion.resolve(Unit))
        }
    }

    private fun callStop(element: Element) {
        val com = element.asDynamic().CONTROL ?: return
        if (com !is Component<*>) return
        async {
            com.onStop(Promise.Companion.resolve(Unit))
        }
    }

    fun <T : HTMLElement> add(element: T, control: (FlexItem.() -> Unit)? = null): T {
        prepareElement(element, control)
        dom.appendChild(element)
        if (started) {
            callStart(element)
        }
        return element
    }

    fun <T : HTMLElement> addBefore(element: T, before: HTMLElement, control: (FlexItem.() -> Unit)? = null): T {
        prepareElement(element, control)
        dom.insertBefore(node = element, child = before)
        if (started) {
            callStart(element)
        }
        return element
    }

    fun <T : HTMLElement> addAfter(element: T, after: HTMLElement, control: (FlexItem.() -> Unit)? = null): T {
        prepareElement(element, control)
        dom.insertBefore(node = element, child = after.nextSibling)
        if (started) {
            callStart(element)
        }
        return element
    }

    fun <T : HTMLElement> remove(element: T): T {
        val el = element
        if (started)
            callStop(element)
        item(element).diatach()
        dom.removeChild(element)
        js("delete el.FLEX_ITEM")
        return element
    }

    enum class Direction(val css: String) {
        Row("row"),
        RowReverse("row-reverse"),
        Column("column"),
        ColumnReverse("column-reverse")
    }

    enum class JustifyContent(val css: String) {
        Start("flex-start"),
        End("flex-end"),
        Center("center"),
        SpaceBetween("space-between"),
        SpaceAround("space-around")
    }

    enum class AlignItems(val css: String) {
        Start("flex-start"),
        End("flex-end"),
        Center("center"),
        Baseline("baseline"),
        Stretch("stretch")
    }

    enum class AlignContent(val css: String) {
        Start("flex-start"),
        End("flex-end"),
        Center("center"),
        SpaceBetween("space-between"),
        SpaceAround("space-around"),
        Stretch("stretch")
    }

    class FlexItem {
        private var dom: HTMLElement? = null

        private var _flexShrink: Int = 1

        var shrink: Int
            get() = _flexShrink
            set(it) {
                _flexShrink = it
                refreshFlex()
            }

        private var _flexBasis: Int? = null
        var basis: Int?
            get() = _flexBasis
            set(it) {
                _flexBasis = it
                refreshFlex()
            }
        private var _flexGrow: Int = 0
        var grow: Int
            get() = _flexGrow
            set(it) {
                _flexGrow = it
                refreshFlex()
            }

        fun refreshFlex() {
            if (dom != null) {
                var f = "${_flexGrow} ${_flexShrink}"
                if (_flexBasis === null)
                    f += " auto"
                else {
                    if (_flexBasis!! <= 0) {
                        f += " ${_flexBasis}%"
                    } else
                        f += " ${_flexBasis}px"
                }

                dom!!.style.flex = f
            }
        }

        private var _alignSelf: AlignSelf = AlignSelf.Auto

        var align: AlignSelf
            get() = _alignSelf
            set(it) {
                _alignSelf = it
                if (dom != null)
                    dom!!.style.alignSelf = it.css
            }

        constructor(dom: HTMLElement) {
            this.dom = dom
            align = align
            refreshFlex()
        }

        fun diatach() {
            dom!!.style.removeProperty("flex")
            dom!!.style.removeProperty("align-self")
        }

        enum class AlignSelf(val css: String) {
            Auto("auto"),
            Start("flex-start"),
            End("flex-end"),
            Center("center"),
            Baseline("baseline"),
            Stretch("stretch")
        }
    }
}

fun Node.nodeIterator() = object : MutableIterator<Node> {
    private var current: Node? = null
    private var started = false
    override fun hasNext(): Boolean {
        if (current != null)
            return true
        if (!started) {
            current = firstChild
            started = true
        }
        return current != null
    }

    override fun next(): Node {
        if (!hasNext())
            throw NoSuchElementException()
        val r = current!!
        current = r.previousSibling
        if (current == null)
            started = false
        return r
    }

    override fun remove() {
        if (current == null)
            throw NoSuchElementException()
        val r = current!!
        current = r.previousSibling
        if (current == null) {
            started = false
        }
        r.parentNode?.removeChild(r)
    }

}

fun <T> MutableIterator<T>.filter(filter: (T) -> Boolean) =
        object : MutableIterator<T> {
            private var nextExist = false
            private var end = false
            private var next: T? = null
            private fun refresh() {
                if (end)
                    return
                if (nextExist)
                    return
                while (this@filter.hasNext()) {
                    next = this@filter.next()
                    if (filter(next as T)) {
                        nextExist = true
                        return
                    }
                }
                end = true
                next = null
            }

            override fun hasNext(): Boolean {
                refresh()
                return nextExist
            }

            override fun next(): T {
                refresh()
                if (nextExist) {
                    nextExist = false
                    return next as T
                } else
                    throw NoSuchElementException()
            }

            override fun remove() {
                this@filter.remove()
                nextExist = false
            }
        }

fun <T, R> MutableIterator<T>.map(func: (T) -> R) = object : MutableIterator<R> {
    override fun hasNext(): Boolean = this@map.hasNext()

    override fun next(): R = func(this@map.next())

    override fun remove() {
        this@map.remove()
    }
}

fun <T> MutableIterator<T>.removeIf(func: (T) -> Boolean): Int {
    var c = 0
    while (hasNext()) {
        if (func(next())) {
            c++
            remove()
        }
    }
    return c
}

fun <T> Iterator<T>.find(func: (T) -> Boolean): T? {
    while (hasNext()) {
        val r = next()
        if (func(r))
            return r
    }
    return null
}

inline fun <reified K : Component<*>> FlexLayout<*>.elementIterator() =
        dom.nodeIterator()
                .filter {
                    it.asDynamic().CONTROL is K
                }
                .map { it.asDynamic().CONTROL.unsafeCast<K>() }