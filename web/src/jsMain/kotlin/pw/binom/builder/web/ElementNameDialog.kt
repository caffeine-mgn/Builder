package pw.binom.builder.web

class ElementNameDialog private constructor(text: String?, job: Boolean) : Dialog("New element") {

    val name = InputString().appendTo(layout, grow = 0, shrink = 0)

    init {
        DivComponent().appendTo(layout)
    }

    val buttons = OkCancelPanel().appendTo(layout, grow = 0, shrink = 0)

    private var cancelled = false

    init {
        dom.style.apply {
            width = "300px"
            height = "130px"
        }
        title = when {
            text == null && job -> "New job"
            text != null && job -> "Edit job"
            text == null && !job -> "New Folder"
            text != null && !job -> "Edit Folder"
            else -> TODO()
        }
        name.text = text ?: ""

        buttons.ok.click {
            close()
        }
        buttons.cancel.click {
            cancelled = true
            close()
        }
    }

    override suspend fun onStart() {
        super.onStart()
        name.dom.focus()
    }

    companion object {
        suspend fun show(text: String?, job: Boolean): String? {
            val dialog = ElementNameDialog(text, job)
            dialog.show()
            if (dialog.cancelled)
                return null
            return dialog.name.text
        }
    }
}