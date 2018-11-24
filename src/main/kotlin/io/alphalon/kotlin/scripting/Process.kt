/*
 * Project: Kotlin Scripting
 * Created: Nov 24, 2018
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
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Executes the [block] on a non-zero exit code and terminates the process.
 */
fun Process?.fail(block: (() -> Unit)? = null) {
    try {
        when {
            this == null -> System.exit(1)
            exitValue() > 0 -> {
                block?.invoke()
                System.exit(exitValue())
            }
        }
    } catch (e: IllegalThreadStateException) {
        println("ERROR: the process has not terminated")
        System.exit(1)
    }
}

/**
 * Executes the [command], waiting for the process to finish.
 */
fun exec(command: List<String>, workingDir: File? = null, waitForMinutes: Long = 60): Process? {
    val builder = ProcessBuilder(command).apply {
        workingDir?.let { directory(it) }
        redirectOutput(ProcessBuilder.Redirect.INHERIT)
        redirectError(ProcessBuilder.Redirect.INHERIT)
    }

    try {
        return builder.start().apply {
            try {
                waitFor(waitForMinutes, TimeUnit.MINUTES)
            } catch (e: InterruptedException) {
                println("WARNING: the process (${command.first()}) is taking longer than $waitForMinutes minutes")
            }
        }
    } catch (e: IOException) {
        println("ERROR: ${e.message}")
    }

    return null
}

/**
 * Executes the [commandLine], performing a terribly naive parsing operation
 * to separate the arguments to pass to the process. Use at your own risk!
 */
fun exec(commandLine: String) = exec(commandLine.split(" "))
