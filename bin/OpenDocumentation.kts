/*
 * Project: kotlin-scripting
 * Script:  OpenDocumentation.kts
 */

//DIR $KO_PROJECT
//CMD openDocumentation - Generates and opens the library API docs

//COMPILER_OPTS -jvm-target 1.8
//DEPS io.alphalon.kotlin:kotlin-scripting:0.1.4

import io.alphalon.kotlin.scripting.*
import java.io.File

val project: GradleProject = currentProject()
project.exec("dokka").fail()

val index = File(Framework.projectDir, "build/dokka/scripting/io.alphalon.kotlin.scripting/index.html")
index.open("The documentation was not generated")
