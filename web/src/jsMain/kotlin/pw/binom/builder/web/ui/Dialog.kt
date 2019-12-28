package pw.binom.builder.web

import org.tlsys.css.CSS
import kotlin.dom.addClass

private val TitleStyle = CSS.style {
    fontFamily = Styles.DEFAULT_FONT
    color = "#fefefe"
    textAlign = "center"
    marginBottom = "20px"
}.name

open class Dialog(title: String = "") : DivComponentWithLayout() {
    private val titleSpan = Span().appendTo(layout, grow = 0, shrink = 0)

    protected fun close() {
        DialogView.close(this)
    }

    protected suspend fun show() {
        DialogView.show(this)
    }

    protected var title: String
        get() = titleSpan.text
        set(value) {
            titleSpan.text = value
        }

    init {
        titleSpan.dom.addClass(TitleStyle)
        this.title = title
        layout.direction = FlexLayout.Direction.Column
        dom.style.apply {
            backgroundColor = "#262626"
            borderTop = "1px solid"
            border = "1px solid #f46800"
            borderImage = "linear-gradient(to right, #ff8100 0%, #993300 75%)"
            borderImageSlice = "1"
            padding = "10px"
            boxShadow = "0 0 50px rgba(244, 104, 0, 0.1)"
        }
    }
}