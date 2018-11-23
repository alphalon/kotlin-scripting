/*
 * Project: Kotlin Scripting
 * Created: Nov 21, 2018
 *
 * Copyright (c) 2018 Alphalon, LLC. All rights reserved.
 */

// Examples of kscript annotations for configuring the runtime environment:
//
// @file:MavenRepository("imagej-releases","http://maven.imagej.net/content/repositories/releases")
// @file:DependsOn("com.beust:klaxon:0.24", "com.github.kittinunf.fuel:fuel:1.3.1")
// @file:Include("util.kt")
// @file:KotlinOpts("-J-Xmx5g")
// @file:CompilerOpts("-jvm-target 1.8")
// @file:EntryPoint("Foo.bar")

//CMD Help Provides information about available commands

@file:DependsOn("io.alphalon.kotlin:kotlin-scripting:0.1-SNAPSHOT")

import io.alphalon.kotlin.scripting.Framework

println("Kotlin Scripting Library ${Framework.libraryVersion}")
println()

// Output related directories
Framework.repo?.let {
    println("Repository:    $it")
}
Framework.project?.let {
    println("Project:       $it")
}
Framework.module?.let {
    println("Module:        $it")
}

// TODO print available commands / scripts