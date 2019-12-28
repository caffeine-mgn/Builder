package pw.binom.builder.web

import pw.binom.builder.web.ace.Ace

class BashEditor(readOnly: Boolean = false, source: String = "") : DivComponent() {
    //    private val div = document.createDiv()
    private val editor = Ace.edit(dom)

    init {
        editor.session.mode = "ace/mode/sh"
        editor.theme = "ace/theme/tomorrow_night"
    }

    var source: String
        get() = editor.session.document.value
        set(value) {
            editor.session.document.value = value
            editor.session.undoManager.reset()
        }
    var readOnly: Boolean
        get() = editor.readOnly
        set(value) {
            editor.readOnly = value
        }

    init {
        this.source = source
        this.readOnly = readOnly
    }
}