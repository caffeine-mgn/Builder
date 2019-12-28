package pw.binom.builder.web

import kotlin.browser.document
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object DialogView {

    private class Record(val con: Continuation<Unit>, val dialog: Dialog) : DivComponent() {
        override suspend fun onStart() {
            super.onStart()
            dialog.onStart()
        }

        override suspend fun onStop() {
            dialog.onStop()
            super.onStop()
        }

        init {
            dialog.dom.style.apply {
                position = "absolute"
                transform = "translate(-50%, -50%)"
                top = "50%"
                left = "50%"
            }
            dom.style.apply {
                position = "fixed"
                top = "0px"
                left = "0px"
                right = "0px"
                bottom = "0px"
                backgroundColor = "rgba(0, 0, 0, 0.75)"
            }
            dom.appendChild(dialog.dom)
        }
    }

    private val layouts = ArrayList<Record>()

    private fun show(record: Record) {
        document.body!!.appendChild(record.dom)
        layouts.add(record)
        async {
            record.onStart()
        }
    }

    fun close(dialog: Dialog) {
        val layout = layouts.find { it.dialog === dialog } ?: return
        async {
            layout.onStop()
            layout.dom.remove()
            layout.con.resume(Unit)
        }
    }

    suspend fun show(dialog: Dialog) {
        suspendCoroutine<Unit> {
            show(Record(con = it, dialog = dialog))
        }
    }
}