/*
 * Project: kotlin-scripting
 * Script:  Publish.kts
 */

//DIR $KO_PROJECT
//CMD publish - Publishes the project after running tests successfully

//DEPS io.alphalon.kotlin:kotlin-scripting:0.1.0

import io.alphalon.kotlin.scripting.*

// Run tests
exec("./gradlew clean test").fail {
    error("there were failing tests")
}

// Build project library and install in local maven repository
exec("./gradlew publishToMavenLocal").fail()
