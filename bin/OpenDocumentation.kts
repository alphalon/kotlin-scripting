/*
 * Project: kotlin-scripting
 * Script:  OpenDocumentation.kts
 */

//DIR $KO_PROJECT
//CMD openDocumentation - Generates and opens the library API docs

//COMPILER_OPTS -jvm-target 1.8
//DEPS io.alphalon.kotlin:kotlin-scripting:0.1.3

import io.alphalon.kotlin.scripting.*
import java.io.File

val project: GradleProject = currentProject()
project.exec("dokka").fail()

val index = File(Framework.projectDir, "build/dokka/scripting/io.alphalon.kotlin.scripting/index.html")
if (index.exists())
    exec("open", index.absolutePath)
else
    error("The documentation was not generated")
