plugins {
    kotlin("jvm") version "1.3.0"
    application
}

buildscript {
    var kotlinCoroutinesVersion: String by extra

    kotlinCoroutinesVersion = "1.0.1"
}

val kotlinCoroutinesVersion: String by extra

group = "io.alphalon.kotlin"
version = "0.1-SNAPSHOT"

application {
    mainClassName = "io.alphalon.kotlin.scripting.MainKt"
}

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinCoroutinesVersion")

    testCompile(kotlin("kotlin-test-junit5"))
}
