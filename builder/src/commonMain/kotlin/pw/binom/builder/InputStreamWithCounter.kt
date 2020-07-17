package pw.binom.builder

import pw.binom.ByteBuffer
import pw.binom.Input

/**
 * Stream for read counter
 */
class InputStreamWithCounter(val stream: Input) : Input {
    private var _counter = 0L

    /**
     * Return count of read bytes
     */
    val counter
        get() = _counter

    override fun close() {
        stream.close()
    }

    override fun read(dest: ByteBuffer): Int {
        val l = stream.read(dest)
        if (l>0)
            _counter+=l
        return l
    }
}

fun Input.withCounter() = InputStreamWithCounter(this)