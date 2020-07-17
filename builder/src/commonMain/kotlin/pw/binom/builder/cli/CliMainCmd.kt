package pw.binom.builder.cli

import pw.binom.builder.*

class CmdRunner : Function() {
    override val description: String?
        get() = "Starter"

    override fun execute(): Result =
            dir(
                    "server" to RunServer(),
                    "node" to RunNode(),
                    "start" to StartJob(),
//                    "nodes" to NodesCmd(),
//                    "tail" to TailCmd(),
                    "cancel" to CancelCmd()
//                    "executes" to ExecutesCmd(),
//                    "tasks" to TasksJob()
            )
}