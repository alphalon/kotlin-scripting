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
import kotlin.test.assertNull

class FrameworkTest {

    @Test
    fun testVersion() {
        // Not set by ko.sh
        assertEquals("", Framework.frameworkVersion)

        // Can't read manifest from within project
        assertEquals("", Framework.libraryVersion)
    }

    @Test
    fun testRuntime() {
        // Shouldn't have access to these when not run through ko.sh
        assertNull(Framework.script)
        assertNull(Framework.searchPath)
    }

    @Test
    fun testDirectories() {
        // Shouldn't have access to these when not run through ko.sh
        assertNull(Framework.runDir)
        assertNull(Framework.repo)
        assertNull(Framework.project)
        assertNull(Framework.module)
    }
}
