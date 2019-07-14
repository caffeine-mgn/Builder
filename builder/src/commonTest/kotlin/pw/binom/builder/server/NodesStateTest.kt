package pw.binom.builder.server

import pw.binom.Platform
import pw.binom.builder.common.ExecuteJob
import pw.binom.builder.common.NodeInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class NodesStateTest {

    @Test
    fun `redeclare idle`() {
        val ns = NodesState()
        val n = NodeInfo(id = "", platform = Platform.JVM, dataCenter = "")
        assertEquals(0, ns.status.size)
        ns.idleNode(n)
        assertEquals(1, ns.status.size)
        ns.idleNode(n)
        assertEquals(1, ns.status.size)
        ns.status.first().also {
            assertNull(it.job)
            assertEquals(it.node, n)
        }
    }

    @Test
    fun `change status idle to execute`() {
        val ns = NodesState()
        val n = NodeInfo(id = "", platform = Platform.JVM, dataCenter = "")

        ns.idleNode(n)
        ns.status.first().also {
            assertNull(it.job)
            assertEquals(it.node, n)
        }
        val j = ExecuteJob(1, "/sleep")
        ns.execute(n, j)
        assertEquals(1, ns.status.size)
        ns.status.first().also {
            assertNotNull(it.job)
            assertEquals(j, it.job)
            assertEquals(it.node, n)
        }
    }


    @Test
    fun `change status execute to finish`() {
        val ns = NodesState()
        val n = NodeInfo(id = "", platform = Platform.JVM, dataCenter = "")
        val j = ExecuteJob(1, "/sleep")
        ns.execute(n, j)
        ns.finish(j)
        assertEquals(0, ns.status.size)
    }
}