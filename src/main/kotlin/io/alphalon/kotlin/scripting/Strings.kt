/*
 * Copyright (c) 2019 Alphalon, LLC. All rights Reserved.
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

package io.alphalon.kotlin.scripting

/**
 * Returns the matched groups from the string.
 */
fun String.find(regex: Regex): List<String> {
    return regex.find(this)?.groupValues?.drop(1) ?: listOf()
}

/**
 * Returns the matched groups from the string.
 */
fun String.find(regex: String): List<String> {
    return find(Regex(regex))
}

/**
 * Conditionally prepends the string representation of [obj] to the string.
 *
 * @param obj The text to be prepended
 * @param pred Whether to prepend the text
 * @param prefix The prefix inserted before the text when prepending
 * @param suffix The suffix inserted after the text when prepending
 */
fun String.prepend(obj: Any?, pred: Boolean = true, prefix: String? = null, suffix: String? = null): String {
    val text = obj?.toString()
    return if (!text.isNullOrBlank() && pred) {
        val sb = if (prefix != null) StringBuilder(prefix).append(text) else StringBuilder(text)
        if (suffix != null) sb.append(suffix)
        sb.append(this)
        sb.toString()
    } else
        this
}

/**
 * Conditionally appends the string representation of [obj] to the string.
 *
 * @param obj The text to be appended
 * @param pred Whether to append the text
 * @param prefix The prefix inserted before the text when appending
 * @param suffix The suffix inserted after the text when appending
 */
fun String.append(obj: Any?, pred: Boolean = true, prefix: String? = null, suffix: String? = null): String {
    val text = obj?.toString()
    return if (!text.isNullOrBlank() && pred) {
        val sb = StringBuilder(this)
        if (prefix != null) sb.append(prefix)
        sb.append(text)
        if (suffix != null) sb.append(suffix)
        sb.toString()
    } else
        this
}

/**
 * Returns a list containing the individual words in the string.
 */
fun String.words(): List<String> = split(Regex("""\s+""")).filter { it.isNotBlank() }
