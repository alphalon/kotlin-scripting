// Project: kotlin-scripting

object Versions {
    const val kotlinCoroutines = "1.0.1"
}

group = "io.alphalon.kotlin"
version = "0.1-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.3.10"
    id("application")
    id("maven-publish")
}

application {
    mainClassName = "io.alphalon.kotlin.scripting.MainKt"
}

sourceSets["test"].java {
    srcDir("scripts")
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("scripting-jvm"))
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.kotlinCoroutines}")

    // implementation(kotlin("scripting-common"))
    // implementation(kotlin("script-util"))

    testCompile(kotlin("test-junit5"))
    testImplementation("com.github.holgerbrandl:kscript-annotations:1.2")
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Implementation-Version"] = version
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets["main"].allSource)
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            artifactId = "kotlin-scripting"

            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}
