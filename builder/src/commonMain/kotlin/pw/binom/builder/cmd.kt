package pw.binom.builder

import pw.binom.MalformedURLException
import pw.binom.URL
import pw.binom.io.file.File
import kotlin.jvm.JvmName
import kotlin.reflect.KProperty

fun execute(args: Array<String>, func: Function) {
    var func = func
    try {
        var i = 0
        LOOP@ while (i < args.size) {
            val item = args[i]
            if (item == "help" || item == "-help") {
                println(func.generateHelp())
                return
            }
            if (item.startsWith("-") && "=" in item) {
                val searchParam = item.substring(0, item.indexOf('=')).removePrefix("-")
                val p = func.params[searchParam]
                        ?: throw InvalidCommandArgumentException("Can't find param \"$searchParam\"")
                p.add(item.substring(item.indexOf('=') + 1))
                i++
                continue
            }

            if (item.startsWith("-")) {
                val flag = func.flags[item.substring(1)]
                if (flag != null) {
                    if (flag.value) {
                        throw InvalidCommandArgumentException("Flag \"-${item}\" already set")
                    }
                    flag.value = true
                    i++
                    continue@LOOP
                }
            }

            func.params.values.forEach {
                it.check()
            }
            val r = func.execute()
            when (r) {
                is Result.Action -> {
                    throw InvalidCommandArgumentException("Unknown command \"$item\"")
                }
                is Result.SubFunctions -> {
                    func = r.functions[item] ?: throw InvalidCommandArgumentException("Can't find function $item")
                    i++
                    continue@LOOP
                }
            }


            i++
        }

        func.params.values.forEach {
            it.check()
        }

        val r = func.execute()
        if (r is Result.Action)
            r.execute()
        if (r is Result.SubFunctions) {
            println("Invalid arguments")
            println(func.generateHelp())
        }
    } catch (e: InvalidCommandArgumentException) {
        print("Invalid Commands: ")
        println(e.message)
        println()
        println(func.generateHelp())
    }
}

sealed class Result {
    abstract class Action : Result() {
        abstract fun execute()
    }

    abstract class SubFunctions : Result() {
        abstract val functions: Map<String, Function>
    }
}

abstract class Function {
    internal val params = HashMap<String, Value<*>>()
    internal val flags = HashMap<String, FlagImpl>()
    abstract val description: String?
    protected fun param(name: String, description: String? = null): Value<String?> {
        val p = RootSingleParam(name, description)
        params[name] = p
        return p
    }

    protected fun paramList(name: String, description: String? = null): Value<List<String>> {
        val p = RootParamList(name = name, description = description)
        params[name] = p
        return p
    }

    protected fun flag(name: String, description: String? = null): Value<Boolean> {
        val f = FlagImpl(name = name, description = description)
        flags[name] = f
        return f
    }

    abstract fun execute(): Result

    internal fun generateHelp(): String {
        val sb = StringBuilder()
        var min = "help".length
        val lines = LinkedHashMap<String, String>()
        (execute() as? Result.SubFunctions)?.let {
            it.functions.forEach {
                lines[it.key] = it.value.description ?: "Execute ${it.key}"
            }
        }

        params.forEach {
            lines["-${it.key}=value"] = it.value.description ?: ""
        }
        lines["help"] = "Print help information"

        lines.forEach {
            min = maxOf(min, it.key.length)
        }

        min += 4
        sb.append("Commands:\n")
        lines.forEach {
            sb.append(it.key)
            for (i in (it.key.length)..min)
                sb.append(" ")
            sb.append(it.value)
            sb.append("\n")
        }
        return sb.toString()
    }

    fun action(func: () -> Unit) = object : Result.Action() {
        override fun execute() {
            func()
        }
    }

    fun dir(vararg functions: Pair<String, Function>) = object : Result.SubFunctions() {
        override val functions: Map<String, Function>
            get() = functions.toMap()

    }

    fun error(message: String): Nothing = throw InvalidCommandArgumentException(message)
}

class InvalidCommandArgumentException(message: String) : RuntimeException(message)

fun <T : Any> Value<T?>.require() = convert<T> {
    if (it == null)
        throw InvalidCommandArgumentException("Require Argument \"-$name\"")
    if (it is List<*> && it.isEmpty())
        throw InvalidCommandArgumentException("At least once argument \"-$name\" must defined")
    it
}

fun <V : Any, T : V> Value<T?>.default(func: (T?) -> V?) = convert {
    if (it == null)
        func(it)
    else
        it
}

@JvmName("requireElements")
fun <T : Any, V : List<T>> Value<V>.require() = convert {
    if (it == null)
        throw InvalidCommandArgumentException("Require Argument \"-$name\"")
    if (it is List<*> && it.isEmpty())
        throw InvalidCommandArgumentException("At least once argument \"-$name\" must defined")
    it
}

interface Value<T> {
    operator fun getValue(thisRef: Any, property: KProperty<*>): T
    fun <E> convert(func: (T) -> E): Value<E>
    val description: String?
    val name: String

    fun add(value: String)
    fun check()
    fun reset()
}

abstract class AbstractValue<T> : Value<T> {
    private val convertors = ArrayList<(Any?) -> Any?>()

    protected abstract val value: T

    private fun value(): T {
        var vv: Any? = value
        convertors.forEach {
            vv = it(vv)
        }

        return vv as T
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): T = value()

    override fun <E> convert(func: (T) -> E): Value<E> {
        this.convertors.add(func as (Any?) -> Any?)
        return this as Value<E>
    }

    override fun check() {
        value()
    }
}

fun Value<String>.file() = convert { File(it) }
fun Value<String>.notBlank() = convert {
    if (it.isBlank())
        throw InvalidCommandArgumentException("Value is blank")
    it
}

fun Value<String>.url() = convert {
    try {
        URL(it)
    } catch (e: MalformedURLException) {
        throw InvalidCommandArgumentException("Invalid url \"$it\"")
    }
}

@JvmName("urlList")
fun Value<List<String>>.url() = convert {
    it.map {
        try {
            URL(it)
        } catch (e: MalformedURLException) {
            throw InvalidCommandArgumentException("Invalid url \"$it\"")
        }
    }
}

fun Value<File>.fileExist() = convert {
    if (!it.isFile)
        throw InvalidCommandArgumentException("File \"$it\" is not exist")
    it
}

fun Value<File>.dirExist() = convert {
    if (!it.isDirectory)
        throw InvalidCommandArgumentException("File \"$it\" is not exist")
    it
}

class FlagImpl(override val description: String?, override val name: String) : AbstractValue<Boolean>() {
    override fun add(value: String) {
        this.value = true
    }

    override fun check() {

    }

    override fun reset() {
        value = false
    }

    public override var value = false
}

internal class RootSingleParam(override val name: String, override val description: String?) : AbstractValue<String?>() {
    override var value: String? = null

    override fun reset() {
        value = null
    }

    override fun add(value: String) {
        if (this.value != null)
            throw InvalidCommandArgumentException("Param \"$name\" already set")
        this.value = value
    }
}

internal class RootParamList(override val description: String?, override val name: String) : AbstractValue<List<String>>() {

    override fun reset() {
        value1.clear()
    }

    override fun add(value: String) {
        this.value1.add(value)
    }

    var value1 = ArrayList<String>()
    override val value: List<String>
        get() = value1
}