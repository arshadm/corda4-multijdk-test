package net.corda.multijdk

import java.io.File

open class MultiJdkTest {

    protected fun generateClassPath(jdkClassifier: String): List<String> {
        val dependenciesDir = System.getProperty("dependencies_dir")
        return File("${dependenciesDir}/${jdkClassifier}").listFiles().map { it.absolutePath }
    }
}
