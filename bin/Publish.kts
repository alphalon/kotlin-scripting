/*
 * Project: kotlin-scripting
 * Script:  Publish.kts
 */

//DIR $KO_PROJECT
//CMD publish - Publishes the project after running tests successfully

//COMPILER_OPTS -jvm-target 1.8
//DEPS io.alphalon.kotlin:kotlin-scripting:0.1.2-SNAPSHOT

import io.alphalon.kotlin.scripting.*

val repo: GitRepository = currentRepo()
val project: GradleProject = currentProject()

// Check version
if (project.version?.endsWith("-SNAPSHOT") != false)
    error("Cannot publish a snapshot version, try 'ko updateVersion'?")

// Make sure there are no changed files
if (repo.hasChanges())
    error("Cannot publish with changed files in repository")

// Run tests
project.clean()
if (!project.test())
    error("Cannot publish with failing tests")

// Add a version tag to git
repo.tag("v${project.version}").fail()

// Build project library and install in local maven repository
project.exec("publishToMavenLocal").fail()
