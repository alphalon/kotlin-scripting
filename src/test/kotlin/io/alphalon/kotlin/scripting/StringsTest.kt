/*
 * Copyright (c) 2019 Alphalon, LLC. All rights Reserved.
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

import kotlin.test.Test
import kotlin.test.assertEquals

class StringsTest {

    @Test
    fun `find text with single match`() {
        val found = "There are 30 cows in the field".find("(\\d+)")
        assertEquals(1, found.size)
        assertEquals("30", found.first())
    }

    @Test
    fun `find text with multiple matches`() {
        val found = "There are 30 cows in the field".find("(\\d+)\\s+([a-z]*)")
        assertEquals(2, found.size)
        assertEquals("30", found.first())
        assertEquals("cows", found[1])
    }

    @Test
    fun `prepend string`() {
        assertEquals("2base", "base".prepend(2))
        assertEquals("3_base", "base".prepend(3, suffix = "_"))
        assertEquals(":4_base", "base".prepend(4, prefix = ":", suffix = "_"))
        assertEquals("base", "base".prepend(5, pred = false, prefix = "_", suffix = "_"))
        assertEquals("base", "base".prepend(null, pred = true, prefix = "_", suffix = "_"))
    }

    @Test
    fun `append string`() {
        assertEquals("base_framed", "base".append("_framed"))
        assertEquals("base_framed", "base".append("framed", prefix = "_"))
        assertEquals("base_framed.png", "base".append("framed", prefix = "_", suffix = ".png"))
        assertEquals("base", "base".append("framed", pred = false, prefix = "_", suffix = ".png"))
        assertEquals("base", "base".append(null, pred = true, prefix = "_", suffix = ".png"))
    }

    @Test
    fun `parse string into words`() {
        assertEquals(listOf(), "".words())
        assertEquals(listOf("one"), "one".words())
        assertEquals(listOf("one", "two"), "one two".words())
        assertEquals(listOf("one", "two", "three"), "one two three".words())

        // Don't break on hyphens
        assertEquals(listOf("one-two"), "one-two".words())
    }

    @Test
    fun `parse string with multiple lines into words`() {
        assertEquals(listOf("one", "two"), "one\ntwo".words())
    }
}
