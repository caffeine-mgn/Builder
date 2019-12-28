package pw.binom.builder.web

import org.w3c.dom.*
import org.w3c.dom.events.Event
import pw.binom.io.Closeable

fun Document.createDiv() = createElement("div").unsafeCast<HTMLDivElement>()
fun Document.createLink() = createElement("a").unsafeCast<HTMLAnchorElement>()
val HTMLElement.childs
    get() = Array(childElementCount) { childNodes.get(it) }

fun <T : Node> T.on(event: String, func: (Event) -> Unit): Closeable {
    addEventListener(event, func)
    return object : Closeable {
        override fun close() {
            removeEventListener(event, func)
        }
    }
}

class NodeIterator(start: Node?) : Iterator<Node> {
    private var current = start
    override fun hasNext(): Boolean = current != null

    override fun next(): Node {
        if (current == null)
            throw NoSuchElementException()
        var c = current!!
        current = c.nextSibling
        return c
    }
}

fun NodeList.asSequence(): Sequence<Node> =
        if (length == 0)
            emptySequence()
        else
            Sequence { NodeIterator(get(0)) }

/*
class NodeIterator(val parent: NodeList) : Iterator<Node> {
    override fun hasNext(): Boolean = index < parent.length - 1

    override fun next(): Node {
        if (index >= parent.length)
            throw NoSuchElementException()
        return parent[index++]!!
    }

    var index = 0
}
*/


inline fun encodeURIComponent(uri: String): String = js("encodeURIComponent(uri)")
inline fun decodeURIComponent(uri: String): String = js("decodeURIComponent(uri)")

fun String.decodeUrl() = split('/').map { decodeURIComponent(it) }.joinToString("/")
fun String.encodeUrl() = split('/').map { encodeURIComponent(it) }.joinToString("/")