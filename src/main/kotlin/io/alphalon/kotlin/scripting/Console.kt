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

import kotlin.math.max
import kotlin.streams.toList

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
    exit(usage.trimIndent())
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
fun echo(message: Any? = null) {
    if (!quietMode) {
        if (message != null && message.toString().isNotBlank()) {
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
    exit(errorMessage(message), exitCode)
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
fun addTableRow(vararg columns: Any) {
    synchronized(capturedTable) {
        if (columns.isNotEmpty()) {
            capturedTable.add(columns.map { it.toString() })
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

/**
 * Returns the best guess of the number of columns and rows in a terminal window.
 *
 * If these values could not be determined, returns 80x24.
 *
 * @returns A pair of columns and rows
 */
fun consoleDimensions(): Pair<Int, Int> {
    try {
        // Try tput (may not be installed)
        try {
            val rows = execLines("tput lines").firstOrNull()
            val columns = execLines("tput cols").firstOrNull()
            if (rows != null && columns != null)
                return Pair(columns.toInt(), rows.toInt())
        } catch (e: NumberFormatException) {
            // Try next method
        }

        // Try stty (may not recognize stdin as terminal)
        try {
            execLines("stty size").firstOrNull()?.let { Regex("""(\d*)\s*(\d*)""").find(it) }?.groupValues?.let { values ->
                return Pair(values[2].toInt(), values[1].toInt())
            }
        } catch (e: NumberFormatException) {
            // Try next method
        }

        // Consult environment variables (passed from ko.sh)
        val rows = System.getenv("LINES")
        val columns = System.getenv("COLUMNS")
        if (rows != null && columns != null)
            return Pair(columns.toInt(), rows.toInt())
    } catch (e: Exception) {
        // Return default value
    }

    return Pair(80, 24)
}
