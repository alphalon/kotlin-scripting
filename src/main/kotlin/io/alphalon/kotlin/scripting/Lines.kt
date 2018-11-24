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
