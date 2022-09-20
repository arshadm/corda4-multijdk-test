package net.corda.multijdk

import net.corda.core.utilities.getOrThrow
import net.corda.node.services.Permissions
import net.corda.testing.core.ALICE_NAME
import net.corda.testing.core.BOB_NAME
import net.corda.testing.core.DUMMY_NOTARY_NAME
import net.corda.testing.driver.DriverParameters
import net.corda.testing.driver.NodeParameters
import net.corda.testing.driver.driver
import net.corda.testing.node.NotarySpec
import net.corda.testing.node.User
import net.corda.testing.node.internal.findCordapp
import org.junit.Test

class BankOfCordaTest : MultiJdkTest() {

    @Test(timeout = 300_000)
    fun `Two nodes both running JDK 11, with cordapps also targeting JDK 11`() {
        val java11Home = System.getProperty("jdk11_home")
        val jdk11ClassPath = generateClassPath("jdk11")
        val cordapps = listOf(findCordapp(scanPackage = "net.corda.finance.flows", archiveAppendix = "jdk11"))

        val user = User("mark", "dadada", setOf(Permissions.all()))
        driver(
            DriverParameters(
                isDebug = false, startNodesInProcess = false,
                notarySpecs = listOf(NotarySpec(name = DUMMY_NOTARY_NAME, startInProcess = false)),
                notaryCustomOverrides = mapOf("javaHome" to java11Home, "classPath" to jdk11ClassPath)
            )
        ) {
            val defaultNodeParameters = NodeParameters(
                rpcUsers = listOf(user),
                javaHome = java11Home,
                classPath = jdk11ClassPath,
                additionalCordapps = cordapps
            )

            val nodeAHandle = startNode(providedName = ALICE_NAME, defaultParameters = defaultNodeParameters).getOrThrow()
            val nodeBHandle = startNode(providedName = BOB_NAME, defaultParameters = defaultNodeParameters).getOrThrow()
        }
    }
}
