/*
 * Project: Kotlin Scripting
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

//DEPS io.alphalon.kotlin:kotlin-scripting:0.1-SNAPSHOT

import io.alphalon.kotlin.scripting.*

// Ensure we were called by the ko.sh bash script
if (Framework.script == null) {
    echo("ERROR: cannot determine search path")
    exit(1)
}

fun List<String>.hasFlag(vararg options: String) = intersect(options.toList()).isNotEmpty()
val options = args.filter { it.startsWith("-") }.map(String::toLowerCase)
val arguments = args.filter { !it.startsWith("-") }

val printHelp = options.hasFlag("-h", "--help")
val printPath = options.hasFlag("-f", "--find")
val printDirectories = options.hasFlag("-v", "--verbose")
val filterRepo = options.hasFlag("-r", "--repo")
val filterProject = options.hasFlag("-p", "--project")
val filterModule = options.hasFlag("-m", "--module")

if (printHelp) {
    echo("Usage:")
    echo("  help [options...] [command]")
    echo()
    echo("Prints a list of available commands or detailed information about a specific command.")
    echo()
    echo("Options:")
    echo("  -f, --find       Prints the path of the script associated with the commands")
    echo("  -v, --verbose    Prints the discovered directories")
    echo("  -r, --repo       Limits the output to the repository")
    echo("  -p, --project    Limits the output to the project")
    echo("  -m, --module     Limits the output to the module")
    exit()
}

// Scope results
val top = when {
    filterRepo -> Framework.repo ?: Framework.project
    filterProject -> Framework.project ?: Framework.repo
    filterModule -> Framework.module
    else -> null
}

echo("Kotlin Scripting Library ${Framework.libraryVersion}")

// Output related directories
if (printDirectories) {
    echoSeparator()
    Framework.repo?.let { addTableRow("Repository:", it.absolutePath) }
    Framework.project?.let { addTableRow("Project:", it.absolutePath) }
    Framework.module?.let { addTableRow("Module:", it.absolutePath) }
    echoTable()
}

echoSeparator()
if (arguments.isNotEmpty()) {
    val name = arguments.first()
    val command = searchForCommand(name, top)

    if (command != null) {
        echo("Script: ${command.script}")
        echo()

        if (commandProvidesHelp(command))
            runScript(command, listOf("--help"))
        else
            echo("${command.name} - ${command.description}")
    } else {
        echo("ERROR: could not find help for a command matching '$name'")
        exit(1)
    }
} else {
    val commands = availableCommands(top)
    if (commands.isNotEmpty()) {
        echo("Available commands:")
        commands.forEach {
            addTableRow(it.name, if (printPath) it.script.absolutePath else it.description)
        }
        echoTable()
    } else {
        echo("Unable to locate any commands for this context")
        exit(1)
    }
}
