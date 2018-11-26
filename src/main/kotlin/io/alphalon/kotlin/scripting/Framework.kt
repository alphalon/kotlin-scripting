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

@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package io.alphalon.kotlin.scripting

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.function.BiPredicate
import kotlin.streams.toList

/**
 * Returns whether the [name] matches a Kotlin source file.
 */
private fun kotlinFile(name: String) = name.endsWith(".kts") || name.endsWith(".kt")

/**
 * Returns information about the currently running script called from the `ko`
 * shell script.
 */
object Framework {

    /**
     * The currently executing script called though the `ko` shell script.
     */
    val script: File? by lazy { System.getenv("KO_SCRIPT")?.let { File(it) } }

    /**
     * Returns the directory from which the script was run.
     */
    val runDir: File? by lazy { System.getenv("KO_DIR")?.let { File(it) } }

    /**
     * Represents the repository root directory, may be null.
     */
    val repo: File? by lazy { System.getenv("KO_REPO")?.let { File(it) } }

    /**
     * Represents the top-most project directory with a repository or a user's
     * home directory. If no project was found, the current working directory is
     * used as a substitute.
     */
    val project: File? by lazy { System.getenv("KO_PROJECT")?.let { File(it) } }

    /**
     * The module directory closest to the current working directory. Defaults
     * to the project directory if a module was not found.
     */
    val module: File? by lazy { System.getenv("KO_MODULE")?.let { File(it) } }

    /**
     * Returns a collection of directories used to search for the currently
     * running script.
     */
    val searchPath: List<File>? by lazy {
        System.getenv("KO_SEARCH_PATH")?.let { path ->
            path.split(":").map { File(it) }.filter { it.isDirectory }
        }
    }

    /**
     * The version of the scripting framework currently being used. Returns an
     * empty string if the version could not be determined or the script was
     * not called from the `ko` shell script.
     */
    val frameworkVersion: String by lazy { System.getenv("KO_VERSION") ?: "" }

    /**
     * The version of the scripting library currently being used. Returns an
     * empty string if the version could not be determined.
     */
    val libraryVersion: String by lazy {
        Package.getPackage("io.alphalon.kotlin.scripting").implementationVersion ?: ""
    }
}

data class Command(val name: String, val description: String, val script: File)

/**
 * Returns the commands listed in a script [file].
 */
fun commandsInFile(file: File): List<Command> =
    file.bufferedReader().useLines { lines ->
        val regex = Regex("""^\s*//CMD\s*([-\w]*)\s*-?\s*(.*)""")
        lines.mapNotNull { line ->
            val groups = regex.find(line)?.groups
            val name = groups?.get(1)?.value
            name?.let { Command(it, groups[2]?.value ?: "", file) }
        }.toList()
    }

/**
 * Returns the commands stored in the [directory].
 */
fun commandsInDirectory(directory: File): List<Command> = directory
    .listFiles { _, name -> name.endsWith(".kts") }
    ?.filter { it.name != "ko.kts" }
    ?.map { file ->
        val scriptName = file.name.removeSuffix(".kts")
        commandsInFile(file)
            .filter { it.name.equals(scriptName, ignoreCase = true) }
            .map { Command(it.name, it.description, file) }
            .firstOrNull() ?: Command(scriptName, "", file)
    } ?: listOf()

/**
 * Returns a list of available commands based on the
 * [search path][Framework.searchPath]. The list may be empty if the search path
 * could not be determined or no scripts could be found.
 *
 * If supplied, the commands must reside within the [ancestor] directory or any
 * of its descendants.
 */
fun availableCommands(ancestor: File? = null): List<Command> =
    Framework.searchPath?.let { path ->
        path.flatMap { file ->
            when {
                file.isDirectory -> commandsInDirectory(file)
                file.isFile -> commandsInFile(file)
                else -> listOf()
            }
        }
            .filter { ancestor == null || it.script.isDescendant(ancestor) }
            .sortedBy { it.name.toLowerCase() }
    } ?: listOf()

/**
 * Returns whether the specified [command] supports the --help option to print
 * usage information.
 */
fun commandProvidesHelp(command: Command): Boolean =
    command.script.bufferedReader().useLines { lines ->
        val regex = Regex("""^\s*//HELP.*$""")
        lines.any { regex.matches(it) }
    }

/**
 * Returns the first command that matches the [name]. If an [ancestor] directory
 * is provided, the search will be limited to its descendants.
 */
fun searchForCommand(name: String, ancestor: File? = null): Command? {
    val commands = availableCommands(ancestor).filter { it.name.startsWith(name, ignoreCase = true) }
    if (commands.isEmpty())
        return null

    // Return a single match
    if (commands.count() == 1)
        return commands.first()

    // Return the first exact match
    val exactMatches = commands.filter { it.name.equals(name, ignoreCase = true) }
    if (exactMatches.isNotEmpty())
        return exactMatches.first()

    // Return partial match if they all match to the same command
    val commandName = commands.first().name
    commands.drop(1).forEach {
        if (it.name != commandName)
            return null
    }

    return commands.firstOrNull()
}

/**
 * Runs the script represented by the [command] in an external process,
 * returning its exit code.
 */
fun runScript(command: Command, args: List<String> = listOf()): Int {
    val cmd = listOf("ko", command.name) + args
    return exec(cmd)?.exitValue() ?: 1
}

/**
 * Finds all scripts located in the specified or current [directory] and the
 * immediate search directories.
 */
fun findNearbyScripts(directory: File = File(System.getProperty("user.dir"))): List<File> {
    val dirs = listOf(directory) + System.getenv("KO_SEARCH_DIRS").split(":").map { File(directory, it) }
    return dirs.fold(listOf()) { scripts, dir ->
        val found = dir.listFiles { file -> file.isFile && kotlinFile(file.name) }
        if (found != null) scripts + found else scripts
    }
}

/**
 * Finds all the Kotlin source and scripts below the [scope] directory.
 */
fun findAllScriptsWithinScope(scope: File): List<File> {
    return Files.find(Paths.get(scope.absolutePath), 100, BiPredicate { path, _ ->
        kotlinFile(path.fileName.toString())
    }).map { it.toFile() }.filter { it.isFile }.toList()
}

/**
 * Represents a single declared dependency in a script.
 */
data class Dependency(val script: File, val group: String, val artifact: String, val version: String) {
    val library
        get() = "$group:$artifact"
    val spec
        get() = "$group:$artifact:$version"
}

/**
 * Returns the dependencies declares in the [script] file.
 */
fun findScriptDependencies(script: File): List<Dependency> {
    val regex = Regex("""^\s*//DEPS\s*(.*)|^\s*@file:DependsOn\((.*)\)""")

    return script.bufferedReader().useLines { lines ->
        lines.mapNotNull {
            val groups = regex.find(it)?.groups
            groups?.get(1)?.value ?: groups?.get(2)?.value
        }.toList()
            .flatMap { it.split(",") }
            .map { it.trim(' ', '"') }
            .map { it.split(":") }
            .mapNotNull {
                if (it.count() == 3)
                    Dependency(script, it[0].trim(), it[1].trim(), it[2].trim())
                else
                    null
            }
    }
}
