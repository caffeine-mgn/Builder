package pw.binom.builder.web
/*
import pw.binom.builder.Topic
import pw.binom.builder.common.Event
import pw.binom.builder.server.PathHandler
import pw.binom.builder.server.endsWith
import pw.binom.builder.server.method
import pw.binom.builder.server.plus
import pw.binom.io.ClosedException
import pw.binom.io.IOException
import pw.binom.io.utf8Appendable
import pw.binom.json.jsonNode
import pw.binom.logger.Logger
import pw.binom.logger.info

class EventHandler(val eventTopic: Topic<Event>) : PathHandler() {
    private val LOG = Logger.getLog("/events")

    init {
        filter(method("GET") + endsWith("/tail")) { r, q ->
            q.status = 200

            val writer = q.output.utf8Appendable()
            while (true) {
                try {
                    val event = eventTopic.wait()
                    LOG.info("Send Event ${event}")
                    jsonNode(writer) {
                        event.write(this)
                    }
                    writer.append('\n')
                    q.output.flush()
                } catch (e: ClosedException) {
                    break
                } catch (e: IOException) {
                    break
                }
            }
        }
    }
}
*/