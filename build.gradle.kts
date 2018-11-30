// Project: kotlin-scripting

import org.jetbrains.dokka.gradle.DokkaTask

group = "io.alphalon.kotlin"
version = "0.1.3"

object Versions {
    const val kotlinCoroutines = "1.0.1"
}

plugins {
    kotlin("jvm") version "1.3.10"
    id("maven-publish")
}

buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:0.9.17")
    }
}

apply {
    plugin("org.jetbrains.dokka")
}

// Add scripts to test sources for code completion
sourceSets {
    test {
        java {
            srcDir("bin")
            srcDir("scripts")
        }
    }
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("scripting-jvm"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.kotlinCoroutines}")

    testImplementation(kotlin("test-junit5"))
    testImplementation("com.github.holgerbrandl:kscript-annotations:1.2")

    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.0.0")
}

// Remove IntelliJ build dir
tasks.clean {
    delete("out")
}

// Configure Kotlin compiler
tasks.compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
}
tasks.compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
}

// Configure tests to run using JUnit 5 and output summary
val printTestResult: KotlinClosure2<TestDescriptor, TestResult, Void>
    get() = KotlinClosure2({ desc, result ->
        // Match top-level suite
        if (desc.parent == null)
            println("Results: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} successes, ${result.failedTestCount} failures, ${result.skippedTestCount} skipped)")

        null
    })

tasks.test {
    useJUnitPlatform()
    afterSuite(printTestResult)
}

// Add Implementation-Version for access by the Scripting Library
tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to "KO - Kotlin Scripting",
            "Implementation-Version" to version
        )
    }
}

// Publish with sources to assist in writing scripts
task<Jar>("sourcesJar") {
    classifier = "sources"
    from(sourceSets.main.get().allJava)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "kotlin-scripting"

            from(components["java"])
            artifact(tasks["sourcesJar"])
        }
    }
}

tasks.withType<DokkaTask> {
    jdkVersion = 8
}
