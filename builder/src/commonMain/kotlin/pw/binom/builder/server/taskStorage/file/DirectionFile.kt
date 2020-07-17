package pw.binom.builder.server.taskStorage.file

import pw.binom.builder.server.taskStorage.TaskStorage
import pw.binom.io.file.File

class DirectionFile(override val file: File, override val taskStorage: TaskStorageFile) : AbstractEntityHolderFile(), TaskStorage.Direction