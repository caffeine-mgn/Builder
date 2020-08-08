package pw.binom.builder.web

import pw.binom.builder.web.services.UserService

class ErrorDialog(text: String) : Dialog("Error") {

    private val text = Span(text).appendTo(layout, grow = 0, shrink = 0)

    private val buttons = OkCancelPanel(cancel = null, ok = "OK").appendTo(layout, grow = 0, shrink = 0)

    init {
        buttons.ok.click {
            close()
        }
    }

    companion object {
        suspend fun show(text: String) {
            val d = ErrorDialog(text)
            d.show()
        }
    }
}

object LoginPage : DivComponent() {
    init {
        dom.style.backgroundImage = "url('/web/light-background.jpg')"
        dom.style.height = "100%"
    }

    private val loginWindow = DivLayout(direction = FlexLayout.Direction.Column)
    private val login = InputString().appendTo(loginWindow.layout)
    private val password = InputString().appendTo(loginWindow.layout)
    private val loginBtn = Button("Login").appendTo(loginWindow.layout)

    init {
        dom.append(loginWindow.dom)
        loginWindow.dom.style.apply {
            position = "absolute"
            left = "50%"
            top = "50%"
            transform = "translate(-50%,-50%)"
            backgroundSize = "cover"
        }

        loginBtn.click {
            async {
                val user = UserService.login(login.text, password.text)
                if (user == null) {
                    ErrorDialog.show("Invalid login or password")
                } else {
                    initUi(user)
                }
            }
        }
    }

    override suspend fun onStart() {
        super.onStart()
        val user = UserService.whoIAm()
        if (user != null) {
            initUi(user)
        }
    }
}
