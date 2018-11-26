/*
 * Project: kotlin-scripting
 * Script:  TestReport.kts
 */

//CMD testReport - Opens the test report in a browser

//DEPS io.alphalon.kotlin:kotlin-scripting:0.1.0

import io.alphalon.kotlin.scripting.*
import java.io.File

val index = File(Framework.project, "build/reports/tests/test/index.html")
if (index.exists())
    exec("open $index")
else
    error("could not find test report")
