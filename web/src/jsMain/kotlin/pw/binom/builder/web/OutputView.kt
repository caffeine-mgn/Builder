package pw.binom.builder.web

import org.tlsys.css.CSS
import kotlin.dom.addClass

private val LineStyle = CSS.style {
    ":nth-child(2n)" then {
        backgroundColor = "rgba(255, 2555, 255, 0.02)"
    }

    ":hover" then {
        backgroundColor = "rgba(255, 255, 255, 0.06)"
    }
    fontFamily = Styles.DEFAULT_FONT
}.name

private val STDOUTStyle = CSS.style {
    color = "#eaeaea"
}.name

private val STDERRStyle = CSS.style {
    color = "#da151b"
}.name

private val LoadFullStyle = CSS.style {
    color = "#f46800"
    fontFamily = Styles.DEFAULT_FONT
    cursor = "pointer"
}.name

private fun toSize(size: Long): String {
    if (size < 1024)
        return "$size bytes"

    if (size < 1024 * 1024)
        return "${size / 1024} kb"

    return "${size / 1024 / 1024} mb"
}

class OutputView : DivComponentWithLayout() {
    private val scroll = ScrollController(dom)

    private val downloadFull = Span().appendTo(layout, grow = 0, shrink = 0)

    init {
        layout.direction = FlexLayout.Direction.Column
        dom.style.apply {
            padding = "5px"
        }
        downloadFull.dom.style.display = "none"
        downloadFull.dom.addClass(LoadFullStyle)
    }

    fun clear() {
        (0 until dom.childNodes.length)
                .forEach {
                    if (dom !== downloadFull.dom)
                        dom.remove()
                }
    }

    fun setDownloadfull(size: Long, func: suspend () -> String) {
        downloadFull.text = "Download Full Logs (${toSize(size)})"
        downloadFull.dom.style.display = ""
        downloadFull.dom.onclick = {
            async {
                var lastElement = downloadFull.dom
                downloadFull.dom.style.display = "none"
                func().splitToSequence('\n').forEach {
                    val s = buildLogSpan(it)
                    layout.addAfter(s.dom, lastElement) {
                        grow = 0
                        shrink = 0
                    }
                    lastElement = s.dom
                }
                lastElement.remove()
            }
        }
    }

    private fun buildLogSpan(text: String): Span {
        val span = Span(text.removePrefix("STDOUT:").removePrefix("STDERR:"))

        span.dom.addClass(LineStyle)
        when {
            text.startsWith("STDOUT:") -> span.dom.addClass(STDOUTStyle)
            text.startsWith("STDERR:") -> span.dom.addClass(STDERRStyle)
        }
        return span
    }

    fun append(text: String) {
        buildLogSpan(text).appendTo(layout, grow = 0, shrink = 0)
    }

    fun moveDown() {
        scroll.moveToEndV()
    }
}