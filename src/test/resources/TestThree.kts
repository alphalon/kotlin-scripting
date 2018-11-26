/*
 * Project: kotlin-scripting
 * Script:  TestThree.kts
 */

//DIR $PWD
//CMD testThree - Enter short description here

//DEPS io.alphalon.kotlin:kotlin-scripting:0.1-SNAPSHOT, com.github.salomonbrys.kotson:kotson:2.5.0

// Single dependency
@file:DependsOn("io.ktor:ktor-client:1.0.0")

// Multiple dependencies
@file:DependsOn("org.slf4j:slf4j-api:1.7.25", "org.slf4j:slf4j-log4j12:1.7.25")

// Multiline annotations won't work!
@file:DependsOn("com.xenomachina:kotlin-argparser:2.0.7",
    "com.google.code.gson:gson:2.8.5")
