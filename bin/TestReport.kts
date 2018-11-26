/*
 * Project: kotlin-scripting
 * Script:  TestReport.kts
 */

//CMD testReport - Opens the test report in a browser

//DEPS io.alphalon.kotlin:kotlin-scripting:0.1-SNAPSHOT

import io.alphalon.kotlin.scripting.*
import java.io.File

val index = File(env("KO_PROJECT"), "build/reports/tests/test/index.html")
if (index.exists())
    exec("open $index")
else
    error("could not find test report")
