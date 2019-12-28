package pw.binom.builder.web.ace

external class Editor(renderer: VirtualRenderer, session: EditSession) {
    fun selectAll()
    fun destroy()
    fun focus()
    fun setStyle(style: String)
    fun setTheme(theme: String)
    fun redo()
    fun undo()
    fun moveCursorTo(row: Int, column: Int)

    var readOnly: Boolean
        @JsName("getReadOnly")
        get
        @JsName("setReadOnly")
        set

    var session: EditSession
        @JsName("getSession")
        get
        @JsName("setSession")
        set

    var theme: String
        @JsName("getTheme")
        get
        @JsName("setTheme")
        set

    var value: String
        @JsName("getValue")
        get
        @JsName("setValue")
        set

    fun gotoLine(lineNumber: Int, column: Int, animate: Boolean = definedExternally)
}