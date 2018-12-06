/*
 * Project: Kotlin Scripting
 * Script:  Help.kts
 * Created: Nov 21, 2018
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

//CMD help - Provides information about available commands
//HELP

//COMPILER_OPTS -jvm-target 1.8
//DEPS io.alphalon.kotlin:kotlin-scripting:0.1.4-SNAPSHOT

import io.alphalon.kotlin.scripting.*

Framework.require()

// Process arguments
fun List<String>.hasFlag(vararg options: String) = intersect(options.toList()).isNotEmpty()
val options = args.filter { it.startsWith("-") }.map(String::toLowerCase)
val arguments = args.filter { !it.startsWith("-") }

val printHelp = options.hasFlag("-h", "--help")
val printPath = options.hasFlag("-f", "--find")
val printDirectories = options.hasFlag("-v", "--verbose")
val scopeRepo = options.hasFlag("-r", "--repo")
val scopeProject = options.hasFlag("-p", "--project")
val scopeModule = options.hasFlag("-m", "--module")

// Print usage and exit
if (printHelp)
    echoUsage("""
        Usage:
          help [options...] [command]

        Outputs a list of available commands or detailed information about a specific
        command.

        Options:
          -f, --find       Prints the path of the script associated with the commands
          -r, --repo       Scopes the commands to the repository
          -p, --project    Scopes the commands to the project
          -m, --module     Scopes the commands to the module
          -v, --verbose    Prints the discovered directories
    """)

// Scope results
val top = try {
    when {
        scopeRepo -> Framework.repoDir ?: throw RuntimeException("repository")
        scopeProject -> Framework.projectDir ?: throw RuntimeException("project")
        scopeModule -> Framework.moduleDir ?: throw RuntimeException("module")
        else -> null
    }
} catch (e: RuntimeException) {
    error("Could not find ${e.message} scope")
}

echo("Kotlin Scripting Library ${Framework.libraryVersion}")

// Output related directories
if (printDirectories) {
    echoSeparator()
    Framework.repoDir?.let { addTableRow("Repository:", it.absolutePath) }
    Framework.projectDir?.let { addTableRow("Project:", it.absolutePath) }
    Framework.moduleDir?.let { addTableRow("Module:", it.absolutePath) }
    echoTable()
}

echoSeparator()
if (arguments.isNotEmpty()) {
    // Output information for a single command
    val name = arguments.first()

    val command = searchForCommand(name, top)
    if (command != null) {
        addTableRow("Command:", command.name)
        addTableRow("Script:", command.script.absolutePath)
        echoTable()
        echoSeparator()

        if (commandProvidesHelp(command))
            runScript(command, "--help")
        else
            echo(command.description)
    } else
        error("Could not find help for a command matching '$name'")
} else {
    // List available commands
    val commands = availableCommands(top)
    if (commands.isNotEmpty()) {
        echo("Available commands:")
        commands.forEach {
            addTableRow(it.name, if (printPath) it.script.absolutePath else it.description)
        }
        echoTable()
    } else
        error("Unable to locate any commands for this context and scope")
}
