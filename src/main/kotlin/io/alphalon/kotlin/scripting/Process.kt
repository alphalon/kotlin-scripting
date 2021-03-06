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

@file:Suppress("unused")

package io.alphalon.kotlin.scripting

import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.stream.Stream
import kotlin.streams.toList
import kotlin.system.exitProcess

/**
 * Returns the value of the environment variable, which may be overridden by a
 * Java property of the same name.
 *
 * @param name The name of the environment variable.
 */
fun env(name: String): String? {
    return System.getProperty(name) ?: System.getenv(name)
}

/**
 * Immediately exits the script with the [exitCode].
 *
 * @param exitCode The code returned to the calling process
 * @return This function never returns
 */
fun exit(exitCode: Int = 0): Nothing {
    exitProcess(exitCode)
}

/**
 * Echoes the [message] to the console and terminates the script with the
 * [exitCode].
 *
 * @param message The message to echo
 * @param exitCode The code returned to the calling process
 * @return This function never returns
 */
fun exit(message: String, exitCode: Int = 0): Nothing {
    echo(message)
    exitProcess(exitCode)
}

/**
 * For a non-zero exit code, executes the [block] and terminates the process.
 *
 * Does nothing when the [Process] completes successfully.
 *
 * @param message A console message to echo in case of failure
 * @param block A action to take before termination
 */
fun Process.fail(message: String? = null, block: (() -> Unit)? = null) {
    try {
        when {
            exitValue() > 0 -> {
                if (message != null)
                    echo(errorMessage(message))

                block?.invoke()
                exit(exitValue())
            }
        }
    } catch (e: IllegalThreadStateException) {
        error("The process has not terminated")
    }
}

/**
 * Parses the command line into the executable and it's arguments.
 *
 * TODO: replace naive parsing with something respecting quoted arguments
 *
 * @param commandLine A string containing the command and its arguments
 * @return A list containing the command and its arguments
 */
private fun parseCommandLine(commandLine: String): List<String> {
    return commandLine.words()
}

/**
 * Converts an [obj] into a String argument for [ProcessBuilder].
 */
private fun argumentString(obj: Any?): String? = when (obj) {
    null -> null
    is String -> obj
    is File -> obj.path
    else -> obj.toString()
}

/**
 * Executes the [command], waiting for the process to finish.
 *
 * @param command A list containing the command and its arguments
 * @param workingDir The directory to execute the command, defaults to the current working directory
 * @param console Whether to redirect the standard output to the console
 * @param waitForMinutes The timeout value for the process to terminate
 * @return The Java [Process]
 */
fun exec(command: List<Any?>, workingDir: File? = null, console: Boolean = true, waitForMinutes: Long = 60): Process {
    val builder = ProcessBuilder(command.mapNotNull(::argumentString)).apply {
        workingDir?.let { directory(it) }

        if (console)
            redirectOutput(ProcessBuilder.Redirect.INHERIT)

        redirectError(ProcessBuilder.Redirect.INHERIT)
    }

    try {
        return builder.start().apply {
            try {
                waitFor(waitForMinutes, TimeUnit.MINUTES)
            } catch (e: InterruptedException) {
                warning("the process (${command.first()}) is taking longer than $waitForMinutes minutes")
            }
        }
    } catch (e: IOException) {
        error(e.message.toString())
    }
}

/**
 * Executes the [command], waiting for the process to finish.
 *
 * @param command A list containing the command and its arguments
 * @param workingDir The directory to execute the command, defaults to the current working directory
 * @param console Whether to redirect the standard output to the console
 * @param waitForMinutes The timeout value for the process to terminate
 * @return The Java [Process]
 */
fun exec(vararg command: Any?, workingDir: File? = null, console: Boolean = true, waitForMinutes: Long = 60): Process =
    exec(command.toList(), workingDir, console, waitForMinutes)

/**
 * Executes the [commandLine], waiting for the process to finish.
 *
 * @param commandLine A string containing the command and its arguments
 * @param workingDir The directory to execute the command, defaults to the current working directory
 * @param console Whether to redirect the process's output to the console
 * @param waitForMinutes The timeout value for the process to terminate
 * @return The Java [Process]
 */
fun exec(commandLine: String, workingDir: File? = null, console: Boolean = true, waitForMinutes: Long = 60) =
    exec(parseCommandLine(commandLine), workingDir, console, waitForMinutes)

/**
 * Executes the [command] asynchronously, returning a [Stream] of lines from the output.
 *
 * @param command A list containing the command and its arguments
 * @param workingDir The directory to execute the command, defaults to the current working directory
 * @return The process's standard output and error combined
 */
fun execOutput(command: List<Any?>, workingDir: File? = null): Stream<String> {
    val builder = ProcessBuilder(command.mapNotNull(::argumentString)).apply {
        workingDir?.let { directory(it) }
        redirectErrorStream(true)
    }

    return try {
        builder.start().inputStream.bufferedReader().lines()
    } catch (e: IOException) {
        error(e.message.toString())
    }
}

/**
 * Executes the [command] asynchronously, returning a [Stream] of lines from the output.
 *
 * @param command A list containing the command and its arguments
 * @param workingDir The directory to execute the command, defaults to the current working directory
 * @return The process's standard output and error combined
 */
fun execOutput(vararg command: Any?, workingDir: File? = null): Stream<String> = execOutput(command.toList(), workingDir)

/**
 * Executes the [commandLine] asynchronously, returning a [Stream] of lines from the output.
 *
 * @param commandLine A string containing the command and its arguments
 * @param workingDir The directory to execute the command, defaults to the current working directory
 * @return The process's standard output and error combined
 */
fun execOutput(commandLine: String, workingDir: File? = null): Stream<String> =
    execOutput(parseCommandLine(commandLine), workingDir)

/**
 * Executes the [command], returning a [List] of lines from the output.
 *
 * @param command A list containing the command and its arguments
 * @param workingDir The directory to execute the command, defaults to the current working directory
 * @return The process's standard output and error combined
 */
fun execLines(command: List<Any?>, workingDir: File? = null): List<String> =
    execOutput(command, workingDir).toList()

/**
 * Executes the [command], returning a [List] of lines from the output.
 *
 * @param command A list containing the command and its arguments
 * @param workingDir The directory to execute the command, defaults to the current working directory
 * @return The process's standard output and error combined
 */
fun execLines(vararg command: Any?, workingDir: File? = null): List<String> =
    execLines(command.toList(), workingDir)

/**
 * Executes the [commandLine], returning a [List] of lines from the output.
 *
 * @param commandLine A string containing the command and its arguments
 * @param workingDir The directory to execute the command, defaults to the current working directory
 * @return The process's standard output and error combined
 */
fun execLines(commandLine: String, workingDir: File? = null): List<String> =
    execLines(parseCommandLine(commandLine), workingDir)
