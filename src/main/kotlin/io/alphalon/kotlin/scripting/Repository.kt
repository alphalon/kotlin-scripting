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

@file:Suppress("unused")

package io.alphalon.kotlin.scripting

import java.io.File

/**
 * Represents a source code repository.
 *
 * @property file The repository root directory marker file
 */
open class Repository(val file: File)

/**
 * Represents Git repositories.
 */
open class GitRepository(dir: File) : Repository(dir) {

    /**
     * Returns whether changes are detected in the repository.
     *
     * @return True, whether changes are detected or changes cannot be detected
     */
    fun hasChanges(): Boolean = exec("git status --exit-code", console = false).exitValue() > 0

    /**
     * Creates a new tag or moves an existing tag with the same name.
     */
    fun tag(tag: String) {
        exec("git tag -af '$tag'")
    }
}

internal fun repo(file: File): Repository =
    when (file.name) {
        ".git" -> GitRepository(file)
        else -> Repository(file)
    }
