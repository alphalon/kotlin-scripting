/*
 * Project: kotlin-scripting
 * Script:  Publish.kts
 */

//DIR $KO_PROJECT
//CMD publish - Publishes the project after running tests successfully

//DEPS io.alphalon.kotlin:kotlin-scripting:0.1.1

import io.alphalon.kotlin.scripting.*

// Check version
val project = Framework.project ?: error("could not find project file")
if (project.version?.endsWith("-SNAPSHOT") != false)
    error("cannot publish a snapshot version, try 'ko updateVersion'?")

// Make sure there are no changed files
val repo = Framework.repo as? GitRepository ?: error("could not determine repository")
if (repo.hasChanges())
    error("cannot publish with changed files in repository")

// Run tests
project.clean()
if (!project.test())
    error("cannot publish with failing tests")

// Add a version tag to git
repo.tag("v${project.version}")

// Build project library and install in local maven repository
project.exec("publishToMavenLocal").fail()
