/*
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
 * Returns information about the currently running script when called from the
 * `ko.sh` shell script.
 */
object Framework {

    /**
     * The currently executing script called though the `ko` shell script.
     */
    val script: File? by lazy { env("KO_SCRIPT")?.let { File(it) } }

    /**
     * Returns the directory from which the script was run.
     */
    val runDir: File? by lazy { env("KO_DIR")?.let { File(it) } }

    /**
     * Represents the repository root directory, may be null.
     */
    val repoDir: File? by lazy { env("KO_REPO")?.let { File(it) } }

    /**
     * Represents the top-most project directory within a repository or the
     * home directory.
     */
    val projectDir: File? by lazy { env("KO_PROJECT")?.let { File(it) } }

    /**
     * The module directory closest to the current working directory. Defaults
     * to the project directory if a module was not found.
     */
    val moduleDir: File? by lazy { env("KO_MODULE")?.let { File(it) } }

    /**
     * Returns the [Repository] associated with the repo marker file.
     */
    val repo: Repository? by lazy { env("KO_REPO_FILE")?.let { repo(File(it)) } }

    /**
     * Returns the [Project] associated with the project marker file.
     */
    val project: Project? by lazy { env("KO_PROJECT_FILE")?.let { project(File(it)) } }

    /**
     * Returns a collection of files and directories used to search for commands.
     */
    val searchPath: List<File>? by lazy {
        env("KO_SEARCH_PATH")?.let { path ->
            path.split(":").map { File(it) }
        }
    }

    /**
     * The version of the scripting framework currently being used.
     *
     * Returns an empty string if the version could not be determined or the
     * script was not called from the `ko` shell script.
     */
    val frameworkVersion: String by lazy { env("KO_VERSION") ?: "" }

    /**
     * The version of the scripting library currently being used.
     *
     * Returns an empty string if the version could not be determined.
     */
    val libraryVersion: String by lazy { this.javaClass.`package`.implementationVersion ?: "" }

    /**
     * Exits the script if it was not called via the `ko.sh` shell script.
     */
    fun require() {
        if (script == null)
            error("this script must be called from the framework ko.sh script")
    }
}

/**
 * Returns the typed repository or terminates the script with an error.
 */
inline fun <reified T> currentRepo(): T = Framework.repo as? T ?: error("Expecting ${T::class.java.simpleName}")

/**
 * Returns the typed project or terminates the script with an error.
 */
inline fun <reified T> currentProject(): T = Framework.project as? T ?: error("Expecting ${T::class.java.simpleName}")

/**
 * Represents a command found on the search path.
 *
 * @property name The command name found inside the script or derived from the script filename
 * @property description The command description found inside the script, may be empty
 * @property script The script file
 */
data class Command(val name: String, val description: String, val script: File)

/**
 * Returns the commands listed in a script [file].
 *
 * @param file The script file
 * @return The commands found in the script, may be empty
 * @suppress
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
 *
 * This function does not return commands found in the `ko.kts` file which are
 * normally returned by the [commandsInFile] function.
 *
 * @param directory The directory to search
 * @return The commands found in the directory
 * @suppress
 */
fun commandsInDirectory(directory: File): List<Command> = directory
    .listFiles { _, name -> name.endsWith(".kts") }
    ?.filter { it.name != "ko.kts" }
    ?.mapNotNull { file ->
        val scriptName = file.name.removeSuffix(".kts")
        commandsInFile(file)
            .filter { it.name.equals(scriptName, ignoreCase = true) }
            .map { Command(it.name, it.description, file) }
            .firstOrNull()
    } ?: listOf()

/**
 * Returns a list of available commands based on the
 * [search path][Framework.searchPath].
 *
 * The list may be empty if the search path could not be determined or no
 * scripts could be found.
 *
 * If supplied, the commands must reside within the [ancestor] directory or any
 * of its descendants.
 *
 * @param ancestor The topmost directory to search
 * @return The commands found
 */
fun availableCommands(ancestor: File? = null): List<Command> =
    Framework.searchPath?.let { path ->
        path.flatMap { file ->
            when {
                file.isDirectory -> commandsInDirectory(file)
                file.isFile -> commandsInFile(file)
                else -> listOf()
            }
        }.filter { ancestor == null || it.script.isDescendant(ancestor) }
            .sortedBy { it.name.toLowerCase() }
    } ?: listOf()

/**
 * Returns whether the specified [command] supports the --help option to print
 * usage information.
 *
 * @param command The command to query
 * @return True, if the command supports the --help option
 * @suppress
 */
fun commandProvidesHelp(command: Command): Boolean =
    command.script.bufferedReader().useLines { lines ->
        val regex = Regex("""^\s*//HELP.*$""")
        lines.any { regex.matches(it) }
    }

/**
 * Returns the first command that matches the [name].
 *
 * If an [ancestor] directory is provided, the search will be limited to its
 * descendants.
 *
 * @param name The partial or complete command name to match
 * @param ancestor The topmost directory to search
 * @return The [Command] if a match was made within or below the [ancestor] directory
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
 * Runs the script represented by the [command] name in an external process.
 *
 * The script must be run in an external process since it may require a
 * different classpath or run directory. This has the benefit of providing
 * process isolation.
 *
 * @param command The partial or complete command name to match
 * @param args The arguments to pass to the script
 * @return The Java [Process]
 */
fun runScript(command: String, vararg args: String): Process = exec(listOf("ko", command) + args)

/**
 * Runs the script represented by the [command] in an external process,
 * returning its process.
 *
 * The script must be run in an external process since it may require a
 * different classpath or run directory. This has the benefit of providing
 * process isolation.
 *
 * @param command The [Command] representing the script to execute
 * @param args The arguments to pass to the script
 * @return The Java [Process]
 */
fun runScript(command: Command, vararg args: String): Process = runScript(command.name, *args)

/**
 * Finds all scripts located in the specified or current [directory] and the
 * immediate search directories.
 *
 * @param directory The directory and parent to search, defaults to the current working directory
 * @return The found script files
 * @suppress
 */
fun findNearbyScripts(directory: File = pwd()): List<File> {
    val dirs = listOf(directory) + System.getenv("KO_SEARCH_DIRS").split(":").map { File(directory, it) }
    return dirs.fold(listOf()) { scripts, dir ->
        val found = dir.listFiles { file -> file.isFile && kotlinFile(file.name) }
        if (found != null) scripts + found else scripts
    }
}

/**
 * Finds all the Kotlin source and scripts below the [scope] directory.
 *
 * @param scope The ancestor directory to search
 * @return The found script files
 * @suppress
 */
fun findAllScriptsWithinScope(scope: File): List<File> {
    return Files.find(Paths.get(scope.absolutePath), 100, BiPredicate { path, _ ->
        kotlinFile(path.fileName.toString())
    }).map { it.toFile() }.filter { it.isFile }.toList()
}

/**
 * Represents a single declared dependency in a script.
 *
 * @property script The script file
 * @property group The Maven groupId
 * @property artifact The Maven artifactId
 * @property version The Maven version
 */
data class Dependency(val script: File, val group: String, val artifact: String, val version: String) {

    /**
     * The group:artifact string representing a library.
     */
    val library
        get() = "$group:$artifact"

    /**
     * The group:artifact:version string representing an instance of a library.
     */
    val spec
        get() = "$group:$artifact:$version"
}

/**
 * Returns the dependencies declared in the [script] file.
 *
 * @param script The script file
 * @return The dependencies declared in the script
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
