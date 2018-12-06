/*
 * Project: kotlin-scripting
 * Script:  install.kts
 */

//DIR $KO_PROJECT
//CMD install - Installs the library into the local Maven repo

//COMPILER_OPTS -jvm-target 1.8
//DEPS io.alphalon.kotlin:kotlin-scripting:0.1.4

import io.alphalon.kotlin.scripting.*

// Build project library and install in local maven repository
val project: GradleProject = currentProject()
project.exec("publishToMavenLocal").fail()
