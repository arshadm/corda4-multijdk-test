package net.corda.multijdk

import net.corda.cordapp.java.jdk8.helloworld.flows.HelloWorldFlow.HelloWorldFlowInitiator as Jdk8HelloWorldFlowInitiator
import net.corda.cordapp.java.jdk11.helloworld.flows.HelloWorldFlow.HelloWorldFlowInitiator as Jdk11HelloWorldFlowInitiator
import net.corda.core.messaging.startFlow
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.seconds
import net.corda.node.services.Permissions
import net.corda.testing.core.ALICE_NAME
import net.corda.testing.core.BOB_NAME
import net.corda.testing.core.DUMMY_NOTARY_NAME
import net.corda.testing.core.singleIdentity
import net.corda.testing.driver.DriverParameters
import net.corda.testing.driver.NodeParameters
import net.corda.testing.driver.driver
import net.corda.testing.node.NotarySpec
import net.corda.testing.node.User
import net.corda.testing.node.internal.findCordapp
import org.junit.Test
import java.io.File

class MultiJdkTest {

    @Test(timeout = 300_000)
    fun `Two nodes both running JDK 8, with cordapps also targeting JDK 8`() {
        val jdk8ClassPath = generateClassPath("jdk8")
        val java8Home = System.getProperty("jdk8_home")
        val cordapps = listOf(findCordapp("net.corda.cordapp.java.jdk8.helloworld.flows"))

        val user = User("mark", "dadada", setOf(Permissions.all()))
        driver(
            DriverParameters(
                isDebug = false, startNodesInProcess = false,
                notarySpecs = listOf(NotarySpec(name = DUMMY_NOTARY_NAME, startInProcess = false)),
                notaryCustomOverrides = mapOf("javaHome" to java8Home, "classPath" to jdk8ClassPath)
            )
        ) {
            val defaultNodeParameters = NodeParameters(
                rpcUsers = listOf(user),
                javaHome = java8Home,
                classPath = jdk8ClassPath,
                additionalCordapps = cordapps
            )

            val nodeAHandle = startNode(providedName = ALICE_NAME, defaultParameters = defaultNodeParameters).getOrThrow()
            val nodeBHandle = startNode(providedName = BOB_NAME, defaultParameters = defaultNodeParameters).getOrThrow()
            nodeAHandle.rpc.let {
                val ref = it.startFlow(::Jdk8HelloWorldFlowInitiator, nodeBHandle.nodeInfo.singleIdentity()).returnValue.getOrThrow(20.seconds)
            }
        }
    }

    @Test(timeout = 300_000)
    fun `Two nodes both running JDK 11, with cordapps also targeting JDK 11`() {
        val jdk8ClassPath = generateClassPath("jdk11")
        val java11Home = System.getProperty("jdk11_home")
        val cordapps = listOf(findCordapp("net.corda.cordapp.java.jdk11.helloworld.flows"))

        val user = User("mark", "dadada", setOf(Permissions.all()))
        driver(
            DriverParameters(
                isDebug = false, startNodesInProcess = false,
                notarySpecs = listOf(NotarySpec(name = DUMMY_NOTARY_NAME, startInProcess = false)),
                notaryCustomOverrides = mapOf("javaHome" to java11Home, "classPath" to jdk8ClassPath)
            )
        ) {
            val defaultNodeParameters = NodeParameters(
                rpcUsers = listOf(user),
                javaHome = java11Home,
                classPath = jdk8ClassPath,
                additionalCordapps = cordapps
            )

            val nodeAHandle = startNode(providedName = ALICE_NAME, defaultParameters = defaultNodeParameters).getOrThrow()
            val nodeBHandle = startNode(providedName = BOB_NAME, defaultParameters = defaultNodeParameters).getOrThrow()
            nodeAHandle.rpc.let {
                val ref = it.startFlow(::Jdk11HelloWorldFlowInitiator, nodeBHandle.nodeInfo.singleIdentity()).returnValue.getOrThrow(20.seconds)
            }
        }
    }

    private fun generateClassPath(jdkClassifier: String): List<String> {
        val dependenciesDir = System.getProperty("dependencies_dir")
        return File("${dependenciesDir}/${jdkClassifier}").listFiles().map { it.absolutePath }
    }
}
