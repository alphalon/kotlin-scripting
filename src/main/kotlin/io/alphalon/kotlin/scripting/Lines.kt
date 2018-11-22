/*
 * Project: Kotlin Scripting
 * Created: Nov 12, 2018
 *
 * Copyright (c) 2018 Alphalon, LLC. All rights reserved.
 */

@file:Suppress("unused")

package io.alphalon.kotlin.scripting

/**
 * Returns the regular expression match for strings matching the [pattern]. The
 * [group] parameter specifies which group within the match to return, by
 * default, returns the entire match.
 *
 * Strings that do not match are removed from the result.
 *
 * @param pattern A regular expression that may contain groups.
 * @param group The regex group to map to. Defaults to the entire match.
 */
fun Sequence<String>.mapRegex(pattern: String, group: Int = 0): Sequence<String> {
    val regex = Regex(pattern)
    return mapNotNull { regex.find(it)?.groups?.get(group)?.value }
}

/**
 * Returns the regular expression match for strings matching the [pattern]. The
 * [group] parameter specifies which group within the match to return, by
 * default, returns the entire match.
 *
 * Strings that do not match are removed from the result.
 *
 * @param pattern A regular expression that may contain groups.
 * @param group The regex group to map to. Defaults to the entire match.
 */
fun Iterable<String>.mapRegex(pattern: String, group: Int = 0): List<String> {
    val regex = Regex(pattern)
    return mapNotNull { regex.find(it)?.groups?.get(group)?.value }
}
