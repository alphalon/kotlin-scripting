#!/usr/bin/env kscript

// Script: <file>
//
//DIR $PWD
//CMD <command> <Enter short description here>

@file:DependsOn("io.alphalon.kotlin:kotlin-scripting:<version>")

import io.alphalon.kotlin.scripting.*

println("Hello World from ${Framework.script ?: "<command>"}")
println("Built with Kotlin Scripting ${Framework.libraryVersion}")
