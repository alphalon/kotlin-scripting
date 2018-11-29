/*
 * Project: kotlin-scripting
 * Script:  OpenDocumentation.kts
 */

//DIR $KO_PROJECT
//CMD openDocumentation - Generates and opens the library API docs

//DEPS io.alphalon.kotlin:kotlin-scripting:0.1.1

import io.alphalon.kotlin.scripting.*
import java.io.File

val project = Framework.project ?: error("could not find project file")
project.exec("dokka").fail()

val index = File(Framework.projectDir, "build/dokka/scripting/io.alphalon.kotlin.scripting/index.html")
if (index.exists())
    exec("open $index")
else
    error("the documentation was not generated.")
