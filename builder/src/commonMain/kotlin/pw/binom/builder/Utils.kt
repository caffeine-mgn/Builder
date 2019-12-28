package pw.binom.builder

import pw.binom.io.UTF8


fun String.decodeUrl()=split('/').map { UTF8.urlDecode(it) }.joinToString("/")
fun String.encodeUrl()=split('/').map { UTF8.urlEncode(it) }.joinToString("/")

fun <T>MutableCollection<T>.removeIf(func:(T)->Boolean){
    val it = iterator()
    while (it.hasNext()){
        if (func(it.next()))
            it.remove()
    }
}

fun <T, V> Iterator<T>.map(func: (T) -> V): List<V> {
    val out = ArrayList<V>()
    while (hasNext())
        out += func(next())
    return out
}

fun <T> Iterator<T>.filter(filter: (T) -> Boolean) =
        object : Iterator<T> {
            private var nextExist = false
            private var end = false
            private var next: T? = null
            private fun refresh() {
                if (end)
                    return
                if (nextExist)
                    return
                while (this@filter.hasNext()) {
                    next = this@filter.next()
                    if (filter(next as T)) {
                        nextExist = true
                        return
                    }
                }
                end = true
                next = null
            }

            override fun hasNext(): Boolean {
                refresh()
                return nextExist
            }

            override fun next(): T {
                refresh()
                if (nextExist) {
                    nextExist = false
                    return next as T
                } else
                    throw NoSuchElementException()
            }
        }