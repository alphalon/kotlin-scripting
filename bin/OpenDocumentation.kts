/*
 * Project: kotlin-scripting
 * Script:  OpenDocumentation.kts
 */

//DIR $KO_PROJECT
//CMD openDocumentation - Generates and opens the library API docs

//COMPILER_OPTS -jvm-target 1.8
//DEPS io.alphalon.kotlin:kotlin-scripting:0.2.0-SNAPSHOT

import io.alphalon.kotlin.scripting.*

val project: GradleProject = currentProject()
project.exec("dokka").fail()
project.file("build/dokka/scripting/index.html").open(errorMessage = "The documentation was not generated")
