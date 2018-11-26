/*
 * Project: Kotlin Scripting
 * Created: Nov 21, 2018
 *
 * Copyright (c) 2018 Alphalon, LLC. All rights reserved.
 */

package io.alphalon.kotlin.scripting

import java.io.File
import kotlin.test.*

private val scriptDir = File("src/test/resources")

class FrameworkTest {

    @BeforeTest
    fun configureFramework() {
        System.setProperty("KO_VERSION", "0.1")
        System.setProperty("KO_SEARCH_PATH", "${scriptDir.absolutePath}:${scriptDir.absolutePath}/ko.kts")
    }

    @Test
    fun testVersion() {
        // Not set by ko.sh
        assertEquals("0.1", Framework.frameworkVersion)

        // Can't read manifest from within project
        assertEquals("", Framework.libraryVersion)
    }

    @Test
    fun testNoDependencies() {
        val script = File(scriptDir, "TestOne.kts")
        val deps = findScriptDependencies(script)

        assertTrue(deps.isEmpty())
    }

    @Test
    fun testSingleDependency() {
        val script = File(scriptDir, "TestTwo.kts")
        val deps = findScriptDependencies(script)

        assertEquals(1, deps.count())
        assertEquals(Dependency(script, "io.alphalon.kotlin", "kotlin-scripting", "0.1-SNAPSHOT"), deps[0])
    }

    @Test
    fun testMultipleDependencies() {
        val script = File(scriptDir, "TestThree.kts")
        val deps = findScriptDependencies(script)

        assertEquals(5, deps.count())
        assertTrue(deps.contains(Dependency(script, "io.alphalon.kotlin", "kotlin-scripting", "0.1-SNAPSHOT")))
        assertTrue(deps.contains(Dependency(script, "com.github.salomonbrys.kotson", "kotson", "2.5.0")))
        assertTrue(deps.contains(Dependency(script, "io.ktor", "ktor-client", "1.0.0")))
        assertTrue(deps.contains(Dependency(script, "org.slf4j", "slf4j-api", "1.7.25")))
        assertTrue(deps.contains(Dependency(script, "org.slf4j", "slf4j-log4j12", "1.7.25")))
    }

    @Test
    fun testAvailableCommands() {
        val commands = availableCommands()

        assertEquals(5, commands.count())
        assertTrue(commands.contains(Command("testOne", "Test 1 script", File(scriptDir, "TestOne.kts").absoluteFile)))
        assertTrue(commands.contains(Command("testTwo", "Test with a single dependency", File(scriptDir, "TestTwo.kts").absoluteFile)))
        assertTrue(commands.contains(Command("testThree", "Enter short description here", File(scriptDir, "TestThree.kts").absoluteFile)))
        assertTrue(commands.contains(Command("hello", "Say Hello", File(scriptDir, "ko.kts").absoluteFile)))
        assertTrue(commands.contains(Command("goodbye", "Say Goodbye", File(scriptDir, "ko.kts").absoluteFile)))
    }

    @Test
    fun testSearchForMissingCommand() {
        assertNull(searchForCommand("invalid"))
    }

    @Test
    fun testSearchForAmbiguousCommand() {
        assertNull(searchForCommand("test"))
    }

    @Test
    fun testSearchForSpecificCommand() {
        val command = Command("testOne", "Test 1 script", File(scriptDir, "TestOne.kts").absoluteFile)

        assertEquals(command, searchForCommand("testO"))
        assertEquals(command, searchForCommand("testOn"))
        assertEquals(command, searchForCommand("testOne"))
    }

    @Test
    fun testCommandsInScript() {
        val script = File(scriptDir, "ko.kts").absoluteFile
        val commands = commandsInFile(script)

        assertEquals(2, commands.count())
        assertTrue(commands.contains(Command("hello", "Say Hello", script)))
        assertTrue(commands.contains(Command("goodbye", "Say Goodbye", script)))
    }

    @Test
    fun testCommandsInScriptWithInvalid() {
        val script = File(scriptDir, "TestTwo.kts").absoluteFile
        val commands = commandsInFile(script)

        assertEquals(2, commands.count())
        assertTrue(commands.contains(Command("testTwo", "Test with a single dependency", script)))
        assertTrue(commands.contains(Command("invalid", "INVALID COMMAND", script)))
    }

    @Test
    fun testCommandprovidesHelp() {
        assertTrue(commandProvidesHelp(searchForCommand("hello")!!))
        assertTrue(commandProvidesHelp(searchForCommand("goodbye")!!))
        assertTrue(commandProvidesHelp(searchForCommand("testTwo")!!))

        assertFalse(commandProvidesHelp(searchForCommand("testOne")!!))
        assertFalse(commandProvidesHelp(searchForCommand("testThree")!!))
    }
}
