package pw.binom.builder.web.ace

/**
 * @see <a href="https://ace.c9.io/api/edit_session.html">Docs</a>
 */
external class EditSession {

    var mode: String
        @JsName("getMode")
        get
        @JsName("setMode")
        set

    var value: String
        @JsName("getValue")
        get
        @JsName("setValue")
        set

    var document: Document
        @JsName("getDocument")
        get
        @JsName("setDocument")
        set

    var undoManager: UndoManager
        @JsName("getUndoManager")
        get
        @JsName("setUndoManager")
        set

    fun redo()
    fun undo()


    fun setAnnotations(annotations: Array<Annotation>)
}

class Annotation(
        @JsName("row")
        val row: Int,

        @JsName("column")
        val column: Int,

        @JsName("text")
        val text: String,

        type: Type
) {
    @JsName("type")
    private val _type: String = type.text

    enum class Type(val text: String) {
        Info("info"),
        Error("error"),
        Warning("warning")
    }
}