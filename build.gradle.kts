// Project: kotlin-scripting

object Versions {
    const val kotlinCoroutines = "1.0.1"
}

group = "io.alphalon.kotlin"
version = "0.1-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.3.10"
    id("maven-publish")
}

// Add scripts to test sources for code completion
sourceSets {
    test {
        java {
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
    // compile("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.kotlinCoroutines}")

    testImplementation(kotlin("test-junit5"))
    testImplementation("com.github.holgerbrandl:kscript-annotations:1.2")

    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.0.0")
}

// Remove IntelliJ build dir
tasks.clean {
    delete("out")
}

tasks.test {
    useJUnitPlatform()
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
