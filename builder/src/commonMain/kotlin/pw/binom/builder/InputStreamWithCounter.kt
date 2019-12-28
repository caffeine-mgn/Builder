package pw.binom.builder

import pw.binom.io.InputStream

/**
 * Stream for read counter
 */
class InputStreamWithCounter(val stream: InputStream) : InputStream {
    private var _counter = 0L

    /**
     * Return count of read bytes
     */
    val counter
        get() = _counter

    override fun skip(length: Long): Long {
        val r = stream.skip(length)
        _counter += r
        return r
    }

    override val available: Int
        get() = super.available

    override fun close() {
        stream.close()
    }

    override fun read(data: ByteArray, offset: Int, length: Int): Int {
        val r = stream.read(data, offset, length)
        if (r > 0)
            _counter += r
        return r
    }
}

fun InputStream.withCounter() = InputStreamWithCounter(this)