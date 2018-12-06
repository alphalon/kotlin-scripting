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

package io.alphalon.kotlin.scripting

import java.io.File
import kotlin.test.*

private val scriptDir = File("src/test/resources")

class FrameworkTest {

    @BeforeTest
    fun configureFramework() {
        System.setProperty("KO_VERSION", "0.0.1")
        System.setProperty("KO_SEARCH_PATH", "${scriptDir.absolutePath}:${scriptDir.absolutePath}/ko.kts")
    }

    @Test
    fun `get correct version from framework script`() {
        // Not set by ko.sh
        assertEquals("0.0.1", Framework.frameworkVersion)

        // Can't read manifest from within project
        assertEquals("", Framework.libraryVersion)
    }

    @Test
    fun `return zero dependencies for scripts without any`() {
        val script = File(scriptDir, "TestOne.kts")
        val deps = findScriptDependencies(script)

        assertTrue(deps.isEmpty())
    }

    @Test
    fun `return single script dependency`() {
        val script = File(scriptDir, "TestTwo.kts")
        val deps = findScriptDependencies(script)

        assertEquals(1, deps.count())
        assertEquals(Dependency(script, "io.alphalon.kotlin", "scripting", "0.0.1"), deps[0])
    }

    @Test
    fun `return multiple dependencies found in script`() {
        val script = File(scriptDir, "TestThree.kts")
        val deps = findScriptDependencies(script)

        assertEquals(5, deps.count())
        assertTrue(deps.contains(Dependency(script, "io.alphalon.kotlin", "scripting", "0.0.1")))
        assertTrue(deps.contains(Dependency(script, "com.github.salomonbrys.kotson", "kotson", "2.5.0")))
        assertTrue(deps.contains(Dependency(script, "io.ktor", "ktor-client", "1.0.0")))
        assertTrue(deps.contains(Dependency(script, "org.slf4j", "slf4j-api", "1.7.25")))
        assertTrue(deps.contains(Dependency(script, "org.slf4j", "slf4j-log4j12", "1.7.25")))
    }

    @Test
    fun `return list of available commands found on search path`() {
        val commands = availableCommands()

        assertEquals(5, commands.count())
        assertTrue(commands.contains(Command("testOne", "Test 1 script", File(scriptDir, "TestOne.kts").absoluteFile)))
        assertTrue(commands.contains(Command("testTwo", "Test with a single dependency", File(scriptDir, "TestTwo.kts").absoluteFile)))
        assertTrue(commands.contains(Command("testThree", "Enter short description here", File(scriptDir, "TestThree.kts").absoluteFile)))
        assertTrue(commands.contains(Command("hello", "Say Hello", File(scriptDir, "ko.kts").absoluteFile)))
        assertTrue(commands.contains(Command("goodbye", "Say Goodbye", File(scriptDir, "ko.kts").absoluteFile)))
    }

    @Test
    fun `return null when searching for non-existing command`() {
        assertNull(searchForCommand("invalid"))
    }

    @Test
    fun `return null when searching for ambiguous command`() {
        assertNull(searchForCommand("test"))
    }

    @Test
    fun `return specific command after entering more characters to remove ambiguity`() {
        val command = Command("testOne", "Test 1 script", File(scriptDir, "TestOne.kts").absoluteFile)

        assertEquals(command, searchForCommand("testO"))
        assertEquals(command, searchForCommand("testOn"))
        assertEquals(command, searchForCommand("testOne"))
    }

    @Test
    fun `return all commands found in script in special ko kts script`() {
        val script = File(scriptDir, "ko.kts").absoluteFile
        val commands = commandsInFile(script)

        assertEquals(2, commands.count())
        assertTrue(commands.contains(Command("hello", "Say Hello", script)))
        assertTrue(commands.contains(Command("goodbye", "Say Goodbye", script)))
    }

    @Test
    fun `ensure non-matching command directives are not returned`() {
        val script = File(scriptDir, "TestTwo.kts").absoluteFile
        val commands = commandsInFile(script)

        assertEquals(2, commands.count())
        assertTrue(commands.contains(Command("testTwo", "Test with a single dependency", script)))
        assertTrue(commands.contains(Command("invalid", "INVALID COMMAND", script)))
    }

    @Test
    fun `properly identify whether scripts provide usage help`() {
        assertTrue(commandProvidesHelp(searchForCommand("hello")!!))
        assertTrue(commandProvidesHelp(searchForCommand("goodbye")!!))
        assertTrue(commandProvidesHelp(searchForCommand("testTwo")!!))

        assertFalse(commandProvidesHelp(searchForCommand("testOne")!!))
        assertFalse(commandProvidesHelp(searchForCommand("testThree")!!))
    }
}
