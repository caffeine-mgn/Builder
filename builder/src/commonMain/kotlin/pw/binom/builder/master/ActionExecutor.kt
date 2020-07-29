package pw.binom.builder.master

import pw.binom.SynchronizedQueue
import pw.binom.async
import pw.binom.atomic.AtomicInt
import pw.binom.builder.common.Action
import pw.binom.strong.Strong
import pw.binom.thread.Thread

class ActionExecutor(val strong: Strong) : Thread(), Strong.InitializingBean {

    private val q = SynchronizedQueue<Pair<SlaveService.Slave, Action>>()
    private val lock = AtomicInt(0)

    fun submit(slave: SlaveService.Slave, action: Action) {
        q.push(slave to action)
    }

    override fun run() {
        while (!isInterrupted) {
            if (!lock.compareAndSet(0, 1)) {
                sleep(100)
                if (isInterrupted)
                    break
                continue
            }
            val action = q.pop()
            async {
                try {
                    println("Executing ${action.second::class.simpleName}")
                    action.second.executeMaster(action.first, strong)
                } finally {
                    lock.value = 0
                }
            }
        }
    }

    override fun init() {
        start()
    }
}