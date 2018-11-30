/*
 * Project: kotlin-scripting
 * Script:  UpgradeDependency.kts
 * Created: Nov 25, 2018
 *
 * Copyright (c) 2018 Alphalon, LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
  */

//DIR $PWD
//CMD upgradeDependency - Modifies dependency versions in existing scripts
//HELP

//COMPILER_OPTS -jvm-target 1.8
//DEPS io.alphalon.kotlin:kotlin-scripting:0.1.3

import io.alphalon.kotlin.scripting.*
import java.io.File

Framework.require()

// Process arguments
fun List<String>.hasFlag(vararg options: String) = intersect(options.toList()).isNotEmpty()
val options = args.filter { it.startsWith("-") }.map(String::toLowerCase)
val arguments = args.filter { !it.startsWith("-") }

val printHelp = options.hasFlag("-h", "--help")
val scopeRepo = options.hasFlag("-r", "--repo")
val scopeProject = options.hasFlag("-p", "--project")
val scopeModule = options.hasFlag("-m", "--module")
val dryRun = options.hasFlag("-d", "--dry-run")

setQuietMode(options.hasFlag("-q", "--quiet"))

val version = if (arguments.isNotEmpty()) arguments.first() else Framework.frameworkVersion
val library = if (arguments.count() > 1) arguments[1] else "io.alphalon.kotlin:kotlin-scripting"

// Print usage and exit
if (printHelp)
    echoUsage("""
        Usage:
          upgradeDependency [options...] [version [groupId:artifactId]]

        Upgrades scripts to use a new dependency version. By default, it will
        set the version for the Scripting Library to the latest for the scripts
        located in the current directory and its immediate search directories.

        Options:
          -r, --repo       Upgrade all scripts in the repository
          -p, --project    Upgrade all scripts in the project
          -m, --module     Upgrade all scripts in the module
          -d, --dry-run    Perform a dry run
          -q, --quiet      Quiet mode
    """)

// Validate arguments
if (library.split(":").count() != 2)
    error("The library must specify groupId:artifactId (not $library)")

// Scope results
val scope = try {
    when {
        scopeRepo -> Framework.repoDir ?: throw RuntimeException("repository")
        scopeProject -> Framework.projectDir ?: throw RuntimeException("project")
        scopeModule -> Framework.moduleDir ?: throw RuntimeException("module")
        else -> null
    }
} catch (e: RuntimeException) {
    error("Could not find ${e.message} scope")
}

fun findDependencies(scripts: List<File>, library: String, version: String) = scripts.flatMap { script ->
    findScriptDependencies(script).filter { it.library == library && it.version != version }
}

fun replaceDependency(dependency: Dependency, version: String) {
    dependency.script.replace(dependency.spec, dependency.copy(version = version).spec)
}

echo("Upgrading to $library:$version")

// Find scripts to potentially update
val scripts = if (scope != null)
    findAllScriptsWithinScope(scope)
else
    findNearbyScripts()

// Find scripts to needing the version change
val upgradable = findDependencies(scripts, library, version)
if (upgradable.isEmpty())
    exit("Could not find any scripts to upgrade")

echoSeparator()
if (dryRun) {
    addTableRow("Script", "Current Version")
    upgradable.forEach {
        addTableRow(it.script.absolutePath, it.version)
    }
    echoTable()
} else {
    echo("Upgrading:")
    upgradable.forEachAsync {
        echo(it.script.absolutePath)
        replaceDependency(it, version)
    }
}
