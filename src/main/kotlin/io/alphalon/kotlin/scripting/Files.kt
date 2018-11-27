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

/**
 * Returns true if the file is contained in the [ancestor] directory or any of
 * its subdirectories.
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
