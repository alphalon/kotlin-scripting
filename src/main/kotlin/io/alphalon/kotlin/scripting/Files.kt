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

import java.io.File
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

/**
 * Returns the current working directory.
 *
 * @return The current working directory
 */
fun pwd(): File = File(System.getProperty("user.dir"))

/**
 * Returns true if the file is contained in the [ancestor] directory or any of
 * its subdirectories.
 *
 * @param ancestor The directory to search within
 * @return True, if the file is containing with the ancestor directory
 */
fun File.isDescendant(ancestor: File): Boolean {
    var parent = parentFile
    while (parent != null) {
        if (parent == ancestor)
            return true

        parent = parent.parentFile
    }

    return false
}

/**
 * File types.
 */
enum class FileType {
    ALL,
    FILE,
    DIRECTORY
}

/**
 * Captures files of [type] whose names match the [matcher].
 */
private class FindFileVisitor(val matcher: PathMatcher, val type: FileType) : SimpleFileVisitor<Path>() {

    val files = mutableListOf<File>()

    override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
        if (dir.fileName.toString().startsWith("."))
            return FileVisitResult.SKIP_SUBTREE

        if (matcher.matches(dir) && (type == FileType.ALL || type == FileType.DIRECTORY))
            files.add(dir.toFile())

        return FileVisitResult.CONTINUE
    }

    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
        val matches = matcher.matches(file.fileName)
        if (matches && (type == FileType.ALL || type == FileType.FILE))
            files.add(file.toFile())

        return FileVisitResult.CONTINUE
    }
}

/**
 * Finds descendent files whose filenames match [glob].
 *
 * This function does not recurse into hidden directories and the order of the
 * returned files is unspecified.
 *
 * @param glob The filename matching operator
 * @param type The type of files to return
 * @param maxDepth The maximum depth to traverse
 * @param hidden Whether to search hidden files and directories
 * @return The found files
 */
fun File.find(glob: String, type: FileType = FileType.FILE, maxDepth: Int = Int.MAX_VALUE): List<File> {
    val matcher = FileSystems.getDefault().getPathMatcher("glob:$glob")
    val visitor = FindFileVisitor(matcher, type)
    Files.walkFileTree(this.toPath(), setOf(), maxDepth, visitor)
    return visitor.files
}

/**
 * Finds descendent files whose filenames match [regex].
 *
 * This function does not recurse into hidden directories and the order of the
 * returned files is unspecified.
 *
 * @param regex The regular expression to match filenames
 * @param type The type of files to return
 * @param maxDepth The maximum depth to traverse
 * @param hidden Whether to search hidden files and directories
 * @return The found files
 */
fun File.find(regex: Regex, type: FileType = FileType.FILE, maxDepth: Int = Int.MAX_VALUE): List<File> {
    val matcher = FileSystems.getDefault().getPathMatcher("regex:${regex.pattern}")
    val visitor = FindFileVisitor(matcher, type)
    Files.walkFileTree(this.toPath(), setOf(), maxDepth, visitor)
    return visitor.files
}

/**
 * Replaces all lines in the file matched by the [regex].
 *
 * By default, the entire match will be replaced. A group value less than zero
 * will replace the entire line, while a positive group value will replace
 * that matching group.
 *
 * @param regex The regular expression used for matching
 * @param group The group to replace, defaults to entire match
 * @param replacement The replacement string
 * @return True, if the file was changed
 */
fun File.replace(regex: Regex, group: Int = 0, replacement: String): Boolean {
    val lines = readLines()
    val newLines = lines.replace(regex, group, replacement)
    val changed = newLines != lines

    if (changed)
        writeText(newLines.text())

    return changed
}

/**
 * Replaces all [match] occurrences in the file with [replacement].
 *
 * @param match The literal string to match
 * @param replacement The replacement string
 * @return True, if the file was changed
 */
fun File.replace(match: String, replacement: String): Boolean {
    val lines = readLines()
    val newLines = lines.replace(match, replacement)
    val changed = newLines != lines

    if (changed)
        writeText(newLines.text())

    return changed
}
