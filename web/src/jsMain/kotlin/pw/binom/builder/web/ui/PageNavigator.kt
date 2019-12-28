package pw.binom.builder.web

import org.w3c.dom.HTMLElement
import pw.binom.URL
import kotlin.browser.window

object PageNavigator {
    private lateinit var page: Page
    private lateinit var root: Page

    private fun update() {
        val addr = window.location.href.removePrefix(window.location.protocol)
                .removePrefix(URL(uiUrl).new(protocol = null).toString())
        val items = addr.split('/')
        async {
            val list = ArrayList<BreadCrumbs.Target>(maxOf(0, items.size - 1))
            var url = uiUrl
            page = root
            for (i in 1 until items.size) {
                val it = items[i]
                page = page.next(it) ?: TODO("Page not found $it")
                url = "$url/$it"
                list += BreadCrumbs.Target(url = url, title = page.getTitle())
            }
            BreadCrumbs.setTarget(list)
            PageView.setPage(page)
        }
    }

    fun start(root: Page) {
        this.root = root
        page = root
        window.onpopstate = { event ->
            update()
        }
        update()
    }

    fun goto(url: String) {
        console.info("goto($url)")
        if (window.location.href == url) {
            console.info("not need!")
            return
        }
        console.info("update")
        window.history.pushState(null, "", url)
        update()
    }
}

object PageView {
    private lateinit var page: Page
    private lateinit var body: HTMLElement
    fun start(root: Page, body: HTMLElement) {
        page = root
        this.body = body
        this.body.appendChild(page.dom)
    }

    fun setPage(page: Page) {
        async {
            this.page.onStop()
            body.removeChild(this.page.dom)

            this.page = page
            body.appendChild(this.page.dom)
            this.page.onStart()
        }
    }
}