package pw.binom.builder.web

class HLine : DivComponent() {
    init {
        dom.style.apply {
            background = "linear-gradient(90deg, rgba(255,129,0,1) 0%, rgba(153,51,0,1) 100%)"
            height = "1px"
        }
    }
}