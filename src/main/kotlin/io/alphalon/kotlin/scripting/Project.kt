/*
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
 * Represents a project providing common operations.
 *
 * @property file The project's build file
 */
open class Project(val file: File) {

    /**
     * The declared project version.
     */
    open val version: String? = null

    /**
     * Cleans the project.
     *
     * @return Whether the project was cleaned
     */
    open fun clean(): Boolean = false

    /**
     * Runs the project tests.
     *
     * @return Whether the tests ran successfully
     */
    open fun test(): Boolean = false

    /**
     * Executes the build with the given [arguments][args].
     */
    open fun exec(vararg args: String): Process {
        throw NotImplementedError()
    }

    /**
     * Returns a [File] representing the [path] relative to the project
     * directory.
     */
    fun file(path: String): File = File(file.parentFile, path)
}

/**
 * Represents Gradle projects.
 */
open class GradleProject(file: File) : Project(file) {

    // Use the Gradle wrapper if present
    private val cmd: String = if (File(file.parent, "./gradlew").exists()) "./gradlew" else "gradle"

    override val version: String?
        get() = execOutput("$cmd properties").grep(Regex("""^version: (.*)"""), 1).firstOrNull()

    override fun clean() = exec("clean").exitValue() == 0
    override fun test() = exec("test").exitValue() == 0
    override fun exec(vararg args: String) = exec(listOf(cmd) + args)
}

/**
 * Represents Maven projects.
 */
open class MavenProject(file: File) : Project(file) {

    override fun clean() = exec("mvn clean").exitValue() == 0
    override fun test() = exec("mvn test").exitValue() == 0
    override fun exec(vararg args: String) = exec(listOf("mvn") + args)
}

/**
 * Returns a [Project] appropriate for the specified build [file].
 */
internal fun project(file: File): Project =
    when (file.name) {
        "build.gradle", "build.gradle.kts" -> GradleProject(file)
        "pom.xml" -> MavenProject(file)
        else -> Project(file)
    }
