package pw.binom.builder.web

import org.tlsys.css.CSS

object Styles {
    inline val DEFAULT_FONT
        get() = "'Roboto', sans-serif"

    val ANIMATION_SPEED = 300

    val SIMPLE_TEXT = CSS.style {
        color = "#eaeaea"
        fontFamily = DEFAULT_FONT
    }.name

    val LIST_ITEM = CSS.style {
        textDecoration = "none"
        backgroundColor = "rgba(255,255,255,0.05)"
        padding = "10px"
        margin = "1px 5px"

        transitionDuration = "0.3s"
        transitionProperty = "background-color"

        hover {
            backgroundColor = "rgba(255,255,255,0.1)"
        }
    }.name
}