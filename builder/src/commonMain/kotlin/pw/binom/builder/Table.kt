package pw.binom.builder

class Table {
    private val headers = ArrayList<String>()
    private val rows = ArrayList<List<String>>()

    private fun rowWidth(withHeader: Boolean, index: Int): Int {
        var max = 0
        if (withHeader)
            max = headers[index].length

        rows.forEach {
            max = maxOf(max, it.getOrNull(index)?.length ?: 0)
        }

        return max
    }

    fun addHeader(name: String): Table {
        headers += name
        return this
    }

    fun row(vararg values: String) {
        rows.add(listOf(*values))
    }

    fun print(withHeader: Boolean, out: Appendable) {
        val width = headers.mapIndexed { index, s ->
            rowWidth(withHeader, index)
        }
        if (withHeader) {
            headers.forEachIndexed { index, s ->
                if (index != 0)
                    out.append(" │ ")
                out.append(s)
                if (index == headers.lastIndex) {
                    println()
                } else
                    out.printChar(' ', width[index] - s.length)
            }

            for (x in 0 until headers.size) {
                if (x != 0)
                    out.append("─┼─")

                out.printChar('─', width[x])
                if (x == headers.lastIndex) {
                    println()
                }

            }
        }

        rows.forEachIndexed { rowIndex, row ->
            for (column in 0 until headers.size) {
                if (column != 0)
                    out.append(" │ ")
                val item = row.getOrNull(column)

                if (item==null) {
                    out.printChar(' ', width[column])
                } else {
                    out.append(row[column])
                    out.printChar(' ', width[column] - row[column].length)
                }

                if (column == headers.lastIndex) {
                    out.append('\n')
                }
            }
        }
    }
}

private fun Appendable.printChar(char: Char, count: Int) {
    for (i in 0 until count) {
        append(char)
    }
}

object ConsoleAppendable : Appendable {
    override fun append(c: Char): Appendable {
        print(c)
        return this
    }

    override fun append(csq: CharSequence?): Appendable {
        print(csq)
        return this
    }

    override fun append(csq: CharSequence?, start: Int, end: Int): Appendable {
        csq ?: return this
        (start..end).forEach {
            append(csq[it])
        }
        return this
    }

}