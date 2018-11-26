/*
 * Project: kotlin-scripting
 * Script:  TestReport.kts
 */

//CMD testReport - Opens the test report in a browser

//DEPS io.alphalon.kotlin:kotlin-scripting:0.1-SNAPSHOT

import io.alphalon.kotlin.scripting.*

exec("open ${env("KO_PROJECT")}/build/reports/tests/test/index.html")
