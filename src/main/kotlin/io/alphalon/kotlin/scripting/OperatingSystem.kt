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

import java.awt.Desktop
import java.io.File

/**
 * Generic operating system.
 */
open class OperatingSystem {

    /**
     * Opens a [file] using a preferred or default application.
     *
     * @param file The existing file to open
     * @throws java.awt.HeadlessException
     * @throws java.io.IOException
     * @throws java.lang.UnsupportedOperationException
     */
    open fun open(file: File) = Desktop.getDesktop().open(file)

}

/**
 * Class of UNIX-based operating system.
 */
open class Unix : OperatingSystem()

/**
 * The Mac OS operating system.
 */
class Mac : Unix() {

    override fun open(file: File) {
        exec("open", file.absolutePath)
    }

}

/**
 * The Linux operating system.
 */
class Linux : Unix() {

    override fun open(file: File) {
        exec("xdg-open", file.absolutePath)
    }

}

/**
 * The Windows operating system.
 */
class Windows : OperatingSystem()

/**
 * Returns an instance of the current [operating system][OperatingSystem] that
 * can be used to perform platform-dependent operations.
 */
fun os(): OperatingSystem {
    val os = System.getProperty("os.name")
    return when {
        os.startsWith("Mac OS X") -> Mac()
        os.startsWith("Linux") || os.startsWith("LINUX") -> Linux()
        os.startsWith("Windows") -> Windows()
        else -> OperatingSystem()
    }
}
