/*
 * Project: Kotlin Scripting
 * Created: Nov 21, 2018
 *
 * Copyright (c) 2018 Alphalon, LLC. All rights reserved.
 */

@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package io.alphalon.kotlin.scripting

import java.io.File
import java.nio.file.Files

/**
 * Returns information about the currently running script called from the `ko`
 * shell script.
 */
object Framework {

    internal val searchPaths = listOf("bin", "scripts")

    /**
     * The currently executing script called though the `ko` shell script.
     */
    val script: File? by lazy { System.getenv("KO_SCRIPT")?.let { File(it) } }

    /**
     * Returns the directory from which the script was run.
     */
    val runDir: File by lazy { System.getenv("KO_DIR")?.let { File(it) } ?: File(System.getProperty("user.dir")) }

    /**
     * Represents the repository root directory, may be null.
     */
    val repo: File? by lazy { findRepo() }

    /**
     * Represents the top-most project directory with a repository or a user's
     * home directory. If no project was found, the current working directory is
     * used as a substitute.
     */
    val project: File? by lazy { findProject() }

    /**
     * The module directory closest to the current working directory. Defaults
     * to the project directory if a module was not found.
     */
    val module: File? by lazy { findModule() }

    /**
     * Returns a collection of directories used to search for the currently
     * running script.
     */
    val searchDirs: List<File>? by lazy {
        System.getenv("KO_SEARCH_PATH")?.let {
            it.split(":").map { dir -> File(dir) }
        }
    }

    /**
     * The version of the scripting library currently being used. Returns an
     * empty string if the version could not be determined.
     */
    val libraryVersion: String by lazy {
        Package.getPackage("io.alphalon.kotlin.scripting").implementationVersion ?: ""
    }

    /**
     * Searches from the starting directory upwards to find another directory
     * that contains a marker file or directory.
     */
    private fun searchForMarker(markers: List<String>, start: File = runDir, top: File? = null, first: Boolean = false): File? {
        val upper = top ?: File(System.getProperty("user.home"))
        var found: File? = null
        var dir = start
        do {
            val exists = markers.any { marker ->
                if (marker.contains('*') || marker.contains('?'))
                    Files.newDirectoryStream(dir.toPath(), marker).use { it.iterator().hasNext() }
                else
                    File(dir, marker).exists()
            }

            if (exists) {
                found = dir

                if (first)
                    break
            }

            dir = dir.parentFile ?: break
        } while (dir.toPath().startsWith(upper.toPath()))

        return found
    }

    /**
     * Returns the top-most directory containing a repository marker.
     */
    internal fun findRepo(): File? {
        val markers = listOf(".git", ".svn")
        return searchForMarker(markers)
    }

    /**
     * Returns the top-most directory containing a project marker. The search is
     * restricted to within a repository or the user home directory.
     */
    internal fun findProject(): File? {
        val markers = listOf("build.gradle", "build.gradle.kts", "pom.xml", "project.clj", "*.xcworkspace", "*.xcodeproj", "package.json", "build.boot", "deps.clj", "build.xml", "Makefile", "CMakeLists.txt", "setup.py", "gradlew", "build.sbt", "Gemfile", ".ko_project")
        return searchForMarker(markers, top = repo)
    }

    /**
     * Returns the nearest directory containing a module marker. The search is
     * restricted within a project, and returns `null` if the current directory
     * is not within a project. If the current directory is within a project and
     * a module directory is not found, the project directory will be returned.
     */
    internal fun findModule(): File? {
        val markers = listOf("build.gradle", "build.gradle.kts", "pom.xml", "*.xcodeproj", "package.json", ".ko_module")
        return project?.let { searchForMarker(markers, top = it, first = true) ?: it }
    }
}