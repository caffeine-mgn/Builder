package pw.binom.builder

fun <T>MutableCollection<T>.removeIf(func:(T)->Boolean){
    val it = iterator()
    while (it.hasNext()){
        if (func(it.next()))
            it.remove()
    }
}