package net.corda.multijdk

import net.corda.core.messaging.startFlow
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.seconds
import net.corda.node.services.Permissions
import net.corda.testing.core.ALICE_NAME
import net.corda.testing.core.BOB_NAME
import net.corda.testing.core.DUMMY_NOTARY_NAME
import net.corda.testing.core.singleIdentity
import net.corda.testing.driver.DriverParameters
import net.corda.testing.driver.driver
import net.corda.testing.node.NotarySpec
import net.corda.testing.node.User
import org.junit.Test
import java.io.File

class Jdk8NodesTest {

    @Test(timeout = 300_000)
    fun `Two nodes both running JDK 8, with cordapps also targeting JDK 8`() {
        val jdk8ClassPath = generateClassPath("jdk8")

        val user = User("mark", "dadada", setOf(Permissions.all()))
        driver(
            DriverParameters(
                isDebug = false, startNodesInProcess = false,
                notarySpecs = listOf(NotarySpec(name = DUMMY_NOTARY_NAME, startInProcess = false)),
                notaryCustomOverrides = mapOf("classPath" to jdk8ClassPath)
            )
        ) {

            val nodeAHandle = startNode(providedName = ALICE_NAME, rpcUsers = listOf(user), classPath = jdk8ClassPath).getOrThrow()
            val nodeBHandle = startNode(providedName = BOB_NAME, rpcUsers = listOf(user), classPath = jdk8ClassPath).getOrThrow()
//            nodeAHandle.rpc.let {
//                val ref = it.startFlow(::HelloWorldFlowInitiator, nodeBHandle.nodeInfo.singleIdentity()).returnValue.getOrThrow(20.seconds)
//            }
        }
    }

    private fun generateClassPath(jdkClassifier: String): List<String> {
        val dependenciesDir = System.getProperty("dependencies_dir")
        return File("${dependenciesDir}/${jdkClassifier}").listFiles().map { it.absolutePath }
    }
}
