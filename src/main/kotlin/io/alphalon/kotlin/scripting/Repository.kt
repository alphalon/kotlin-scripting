/*
 * Project: Kotlin Scripting
 * Created: Nov 28, 2018
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

@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package io.alphalon.kotlin.scripting

import java.io.File
import kotlin.streams.toList

/**
 * Represents a source code repository.
 *
 * @property file The repository root directory marker file
 */
open class Repository(val file: File)

/**
 * Provides operations on Git repositories.
 */
open class GitRepository(dir: File) : Repository(dir) {

    private fun statusFiles(pred: (String) -> Boolean): List<File> =
        execOutput("git status --porcelain")
            .filter { pred(it) }
            .map { File(it.substring(3).trim('"')) }
            .toList()

    /**
     * Returns whether there are uncommitted changes in the repository.
     *
     * @return True, if the repository is dirty
     */
    fun isDirty(): Boolean =
        execOutput("git status --porcelain").toList().isNotEmpty()

    /**
     * Returns a list of modified and tracked files that need to be added to
     * the index.
     *
     * @return The list of changes files
     */
    fun changedFiles(): List<File> = statusFiles { it[1] == 'M' }

    /**
     * Returns a list of untracked files.
     *
     * @return The list of files that have not been added
     */
    fun newFiles(): List<File> = statusFiles { it.startsWith("?? ") }

    /**
     * Returns whether changes are detected in the repository.
     *
     * @return True, if there are tracked and modified files
     */
    fun hasChanges(): Boolean =
        changedFiles().isNotEmpty()

    /**
     * Adds a [file] to the index.
     *
     * @param file The file to commit
     * @return The Java [Process]
     */
    fun add(file: File): Process =
        exec("git", "add", file.absolutePath)

    /**
     * Commits the files added to the index with the provided [message].
     *
     * @param message The commit message
     * @return The Java [Process]
     */
    fun commit(message: String): Process =
        exec("git", "commit", "-m", message)

    /**
     * Creates a new tag or moves an existing tag with the same name.
     *
     * @return a Java [Process]
     */
    fun tag(tag: String): Process =
        exec("git", "tag", "-f", tag)
}

internal fun repo(file: File): Repository =
    when (file.name) {
        ".git" -> GitRepository(file)
        else -> Repository(file)
    }
