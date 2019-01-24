/*
 * Copyright (c) 2018 Alphalon, LLC. All rights Reserved.
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

import java.io.File

/**
 * Provides basic user account information.
 */
object User {
    /**
     * User account name.
     */
    val name: String = System.getProperty("user.name")

    /**
     * User home directory.
     */
    val home = File(System.getProperty("user.home"))

    /**
     * User documents directory.
     */
    val documents = File(home, "Documents")

    /**
     * User downloads directory.
     */
    val downloads = File(home, "Downloads")
}
