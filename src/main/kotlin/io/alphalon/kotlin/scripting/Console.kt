/*
 * Project: Kotlin Scripting
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

@file:Suppress("unused")

package io.alphalon.kotlin.scripting

import kotlin.math.max

private var output = false
private var quietMode = false
private val capturedTable: MutableList<List<String>> = mutableListOf()

internal fun warningMessage(message: String) = "WARNING: ${message.decapitalize()}"
internal fun errorMessage(message: String) = "ERROR: ${message.decapitalize()}"

/**
 * Outputs usage information and terminates the script. The indent will be
 * trimmed from the string.
 *
 * @param usage The usage text
 * @return This function does not return
 */
fun echoUsage(usage: String) {
    echo(usage.trimIndent())
    exit()
}

/**
 * Enables or disabled quiet mode.
 *
 * Normal output will not be echoed to the console when quiet mode is enabled,
 * however warnings and errors will always be echoed.
 *
 * @param quiet Whether to enable quiet mode
 */
fun setQuietMode(quiet: Boolean) {
    quietMode = quiet
}

/**
 * Outputs a line to the console.
 *
 * @param message The text to output to the console
 */
fun echo(message: String? = null) {
    if (!quietMode) {
        if (message != null && message.isNotBlank()) {
            println(message)
            output = true
        } else {
            println()
            output = false
        }
    }
}

/**
 * Outputs a blank line to the console if the previous line was not blank.
 */
fun echoSeparator() {
    if (output && !quietMode) {
        println()
        output = false
    }
}

/**
 * Outputs a warning message even if quite mode is enabled.
 *
 * @param message The warning message to output to the console
 */
fun warning(message: String) {
    val wasQuiet = quietMode
    quietMode = false
    echo(warningMessage(message))
    quietMode = wasQuiet
}

/**
 * Outputs an error message and terminates the script.
 *
 * @param message The error message to output to the console
 * @return This function never returns
 */
fun error(message: String, exitCode: Int = 1): Nothing {
    quietMode = false
    echo(errorMessage(message))
    exit(exitCode)
}

/**
 * Adds a row of [columns] to an internal table that is sent to the console
 * via the [echoTable] function.
 *
 * Only one internal table is maintained at a time, and it is cleared of rows
 * when echoed to the console.
 *
 * @param columns The values for each column to output in a single row
 */
fun addTableRow(vararg columns: String) {
    synchronized(capturedTable) {
        if (columns.isNotEmpty()) {
            capturedTable.add(columns.toList())
        }
    }
}

/**
 * Echoes the internal table to the console with left-aligned columns.
 *
 * The actual output is based on rows previously added via the [addTableRow]
 * function. The [separator] provides space between the columns.
 *
 * @param separator The column separator, defaults to four spaces
 */
fun echoTable(separator: String = "    ") {
    synchronized(capturedTable) {
        if (capturedTable.isNotEmpty() && !quietMode) {
            // Determine maximum width for each column
            val numColumns = capturedTable.map { it.count() }.max() ?: 0
            val widths = Array(numColumns) { 0 }
            capturedTable.forEach {
                it.mapIndexed { i, s -> widths[i] = max(widths[i], s.length) }
            }

            // Construct format string
            val format = (0 until numColumns)
                .map { "%-${widths[it]}s" }
                .dropLast(1)
                .joinToString(separator) + "$separator%s"

            // Output each row
            capturedTable.forEach {
                echo(String.format(format, *it.toTypedArray()))
            }
        }

        capturedTable.clear()
    }
}
