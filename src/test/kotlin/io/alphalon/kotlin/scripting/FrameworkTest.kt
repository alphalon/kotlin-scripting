/*
 * Project: Kotlin Scripting
 * Created: Nov 21, 2018
 *
 * Copyright (c) 2018 Alphalon, LLC. All rights reserved.
 */

package io.alphalon.kotlin.scripting

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class FrameworkTest {

    @Test
    fun testVersion() {
        // Can't read manifest from within project
        assertEquals("", Framework.libraryVersion)
    }

    @Test
    fun testDirectories() {
        val dir = File(System.getProperty("user.dir"))
        assertEquals(dir, Framework.repo)
        assertEquals(dir, Framework.project)
        assertEquals(dir, Framework.module)
    }
}
