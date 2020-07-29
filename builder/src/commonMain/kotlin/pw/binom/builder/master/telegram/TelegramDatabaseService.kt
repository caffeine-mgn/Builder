package pw.binom.builder.master.telegram

import pw.binom.io.file.File
import pw.binom.strong.Strong

class TelegramDatabaseService(dataBaseFile: File, val strong: Strong):Strong.InitializingBean{

    override fun init() {
    }

}