/*
 * Project: kotlin-scripting
 * Script:  OpenDocumentation.kts
 */

//DIR $KO_PROJECT
//CMD openDocumentation - Generates and opens the library API docs

//DEPS io.alphalon.kotlin:kotlin-scripting:0.1.1-SNAPSHOT

import io.alphalon.kotlin.scripting.*
import java.io.File

val index = File("build/dokka/scripting/io.alphalon.kotlin.scripting/index.html")

exec("./gradlew dokka").fail()
if (index.exists())
    exec("open $index")
else
    error("the documentation was not generated.")
