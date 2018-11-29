/*
 * Project: Kotlin Scripting
 * Created: Nov 12, 2018
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

import java.util.stream.Stream
import kotlin.streams.toList

/**
 * Returns the lines matching the [regex].
 *
 * By default, returns the entire line. A group value of 0 returns just the
 * portion of the line matching the entire [regex], while groups values
 * greater than zero return that group within the match.
 *
 * @param regex A regular expression that may contain groups.
 * @param group The regex group to return. Defaults to the entire line.
 * @return The matching lines or groups
 */
fun Iterable<String>.grep(regex: Regex, group: Int = -1): List<String> =
    mapNotNull { line ->
        regex.find(line)?.let { result ->
            if (group >= 0 && group < result.groups.count())
                result.groupValues[group]
            else
                line
        }
    }

/**
 * Returns the lines matching the [regex].
 *
 * By default, returns the entire line. A group value of 0 returns just the
 * portion of the line matching the entire [regex], while groups values
 * greater than zero return that group within the match.
 *
 * @param regex A regular expression that may contain groups.
 * @param group The regex group to return. Defaults to the entire line.
 * @return The matching lines or groups
 */
fun Sequence<String>.grep(regex: Regex, group: Int = -1): Sequence<String> =
    mapNotNull { line ->
        regex.find(line)?.let { result ->
            if (group >= 0 && group < result.groups.count())
                result.groupValues[group]
            else
                line
        }
    }

/**
 * Returns the lines matching the [regex].
 *
 * By default, returns the entire line. A group value of 0 returns just the
 * portion of the line matching the entire [regex], while groups values
 * greater than zero return that group within the match.
 *
 * @param regex A regular expression that may contain groups.
 * @param group The regex group to return. Defaults to the entire line.
 * @return The matching lines or groups
 */
@Suppress("UNCHECKED_CAST")
fun Stream<String>.grep(regex: Regex, group: Int = -1): List<String> =
    map { line ->
        regex.find(line)?.let { result ->
            if (group >= 0 && group < result.groups.count())
                result.groupValues[group]
            else
                line
        }
    }.filter { it != null }.toList() as List<String>

/**
 * Filters the collection of strings to those lines containing the [match].
 *
 * @param match The string literal to match
 * @return The matching lines
 */
fun Iterable<String>.grep(match: String): List<String> {
    return filter { it.contains(match) }
}

/**
 * Filters the sequence of strings to those lines containing the [match].
 *
 * @param match The string literal to match
 * @return The matching lines
 */
fun Sequence<String>.grep(match: String): Sequence<String> {
    return filter { it.contains(match) }
}

/**
 * Filters the stream of strings to those lines containing the [match].
 *
 * @param match The string literal to match
 * @return The matching lines
 */
fun Stream<String>.grep(match: String): List<String> {
    return filter { it.contains(match) }.toList()
}

/**
 * Replaces lines matched by the [regex].
 *
 * By default, returns the entire match. A group value less than zero will
 * replace the entire line, while a positive group value will replace that
 * matching group.
 *
 * @param regex The regular expression used for matching
 * @param group The group to replace, defaults to entire match
 * @param replacement The replacement string
 * @return The lines with possible string replacements
 */
fun Iterable<String>.replace(regex: Regex, group: Int = 0, replacement: String): List<String> {
    return map {
        val result = regex.find(it)
        if (result != null) {
            val groups = result.groups
            if (group > 0 && group < groups.count()) {
                it.replace(groups[group]!!.value, replacement)
            } else
                replacement
        } else
            it
    }
}

/**
 * Replaces lines matched by the [regex].
 *
 * By default, returns the entire match. A group value less than zero will
 * replace the entire line, while a positive group value will replace that
 * matching group.
 *
 * @param regex The regular expression used for matching
 * @param group The group to replace, defaults to entire match
 * @param replacement The replacement string
 * @return The lines with possible string replacements
 */
fun Sequence<String>.replace(regex: Regex, group: Int = 0, replacement: String): Sequence<String> {
    return map {
        val result = regex.find(it)
        if (result != null) {
            val groups = result.groups
            if (group > 0 && group < groups.count()) {
                it.replace(groups[group]!!.value, replacement)
            } else
                replacement
        } else
            it
    }
}

/**
 * Replaces lines matched by the [regex].
 *
 * By default, returns the entire match. A group value less than zero will
 * replace the entire line, while a positive group value will replace that
 * matching group.
 *
 * @param regex The regular expression used for matching
 * @param group The group to replace, defaults to entire match
 * @param replacement The replacement string
 * @return The lines with possible string replacements
 */
fun Stream<String>.replace(regex: Regex, group: Int = 0, replacement: String): Stream<String> {
    return map {
        val result = regex.find(it)
        if (result != null) {
            val groups = result.groups
            if (group > 0 && group < groups.count()) {
                it.replace(groups[group]!!.value, replacement)
            } else
                replacement
        } else
            it
    }
}

/**
 * Replaces all [match] occurrences with [replacement] for every line.
 *
 * @return The lines with possible string replacements
 */
fun Iterable<String>.replace(match: String, replacement: String): List<String> {
    return map { it.replace(match, replacement) }
}

/**
 * Replaces all [match] occurrences with [replacement] for every line.
 *
 * @return The lines with possible string replacements
 */
fun Sequence<String>.replace(match: String, replacement: String): Sequence<String> {
    return map { it.replace(match, replacement) }
}

/**
 * Replaces all [match] occurrences with [replacement] for every line.
 *
 * @return The lines with possible string replacements
 */
fun Stream<String>.replace(match: String, replacement: String): Stream<String> {
    return map { it.replace(match, replacement) }
}
