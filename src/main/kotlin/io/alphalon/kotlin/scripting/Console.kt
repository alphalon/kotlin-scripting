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
private val capturedTable: MutableList<List<String>> = mutableListOf()

/**
 * Outputs a line to the console.
 */
fun echo(str: String? = null) {
    if (str != null) {
        println(str)
        output = true
    } else {
        println()
        output = false
    }
}

/**
 * Outputs a blank line to the console if a non-blank line was previously
 * output.
 */
fun echoSeparator() {
    if (output) {
        println()
        output = false
    }
}

/**
 * Adds a row of [columns] to an internal table that is sent to the console
 * via the [echoTable] function. Only one internal table is maintained at a
 * time, and it is cleared of rows when echoed to the console.
 */
fun addTableRow(vararg columns: String) {
    if (columns.isNotEmpty()) {
        capturedTable.add(columns.toList())
    }
}

/**
 * Prints a table with left-aligned columns, if rows have been previously
 * added via the [addTableRow] function. The [separator] provides space
 * between the columns.
 */
fun echoTable(separator: String = "    ") {
    if (capturedTable.isNotEmpty()) {
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
        capturedTable.clear()
    }
}
