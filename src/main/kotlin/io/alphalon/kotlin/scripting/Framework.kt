/*
 * Project: Kotlin Scripting
 * Created: Nov 21, 2018
 *
 * Copyright (c) 2018 Alphalon, LLC. All rights reserved.
 */

@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package io.alphalon.kotlin.scripting

import java.io.File

/**
 * Returns information about the currently running script called from the `ko`
 * shell script.
 */
object Framework {

    /**
     * The currently executing script called though the `ko` shell script.
     */
    val script: File? by lazy { System.getenv("KO_SCRIPT")?.let { File(it) } }

    /**
     * Returns the directory from which the script was run.
     */
    val runDir: File? by lazy { System.getenv("KO_DIR")?.let { File(it) } }

    /**
     * Represents the repository root directory, may be null.
     */
    val repo: File? by lazy { System.getenv("KO_REPO")?.let { File(it) } }

    /**
     * Represents the top-most project directory with a repository or a user's
     * home directory. If no project was found, the current working directory is
     * used as a substitute.
     */
    val project: File? by lazy { System.getenv("KO_PROJECT")?.let { File(it) } }

    /**
     * The module directory closest to the current working directory. Defaults
     * to the project directory if a module was not found.
     */
    val module: File? by lazy { System.getenv("KO_MODULE")?.let { File(it) } }

    /**
     * Returns a collection of directories used to search for the currently
     * running script.
     */
    val searchPath: List<File>? by lazy {
        System.getenv("KO_SEARCH_PATH")?.let { path ->
            path.split(":").map { File(it) }.filter { it.isDirectory }
        }
    }

    /**
     * The version of the scripting framework currently being used. Returns an
     * empty string if the version could not be determined or the script was
     * not called from the `ko` shell script.
     */
    val frameworkVersion: String by lazy { System.getenv("KO_VERSION") ?: "" }

    /**
     * The version of the scripting library currently being used. Returns an
     * empty string if the version could not be determined.
     */
    val libraryVersion: String by lazy { Package.getPackage("io.alphalon.kotlin.scripting").implementationVersion }
}
