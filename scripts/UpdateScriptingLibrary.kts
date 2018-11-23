/*
 * Project: Kotlin Scripting
 * Created: Nov 21, 2018
 *
 * Copyright (c) 2018 Alphalon, LLC. All rights reserved.
 */

//CMD UpdateScriptingLibrary Changes Scripting Library dependency version

@file:DependsOn("io.alphalon.kotlin:kotlin-scripting:0.1-SNAPSHOT")

import io.alphalon.kotlin.scripting.Framework

println("The latest version of the Scripting Library is ${System.getenv("KO_VERSION") ?: Framework.libraryVersion}")
