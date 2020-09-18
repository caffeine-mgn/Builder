package pw.binom.builder.node
//
//import pw.binom.async
//import pw.binom.atomic.AtomicInt
//import pw.binom.concurrency.Exchange
//import pw.binom.concurrency.ExchangeInput
//import pw.binom.concurrency.Worker
//import pw.binom.io.http.websocket.MessageType
//import pw.binom.io.http.websocket.WebSocketConnection
//import pw.binom.io.use
//import pw.binom.io.utf8Appendable
//import kotlin.time.ExperimentalTime
//import kotlin.time.measureTime
//
//class PublishThread(val connection: WebSocketConnection, val actions: ExchangeInput<Action>) {
//    private val worker = Worker()
//
//    init {
//        worker.execute(this) { self ->
//            try {
//                while (!worker.isInterrupted) {
//                    val action = actions.get()
//                    self.connection.write(MessageType.TEXT) {
//                        it.utf8Appendable().use {
//                            it.append(action.toJson())
//                        }
//                    }
//                }
//            } finally {
//                self.worker.requestTermination()
//            }
//        }
//    }
//}