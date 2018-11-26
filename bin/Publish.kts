/*
 * Project: kotlin-scripting
 * Script:  Publish.kts
 */

//DIR $KO_PROJECT
//CMD publish - Publishes the project after running tests successfully

//DEPS io.alphalon.kotlin:kotlin-scripting:0.1-SNAPSHOT

import io.alphalon.kotlin.scripting.*

// Upgrade project scripts
runScript("upgradeDependency", "--quiet", "--project").fail()

// Run tests
exec("./gradlew clean test").fail {
    echo("ERROR: there were failing tests")
}

// Build project library and install in local maven repository
exec("./gradlew publishToMavenLocal").fail()

