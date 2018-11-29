/*
 * Project: kotlin-scripting
 * Script:  UpdateVersion.kts
 */

//DIR $KO_PROJECT
//CMD updateVersion - Updates the project to a new version
//HELP

//DEPS io.alphalon.kotlin:kotlin-scripting:0.1.1

import io.alphalon.kotlin.scripting.*
import java.io.File

// Process arguments
fun List<String>.hasFlag(vararg options: String) = intersect(options.toList()).isNotEmpty()

val options = args.filter { it.startsWith("-") }.map(String::toLowerCase)
val arguments = args.filter { !it.startsWith("-") }

val printHelp = options.hasFlag("-h", "--help")
var version = arguments.firstOrNull()

// Print usage and exit
if (printHelp)
    echoUsage("""
        Usage:
          updateVersion [version]

        Updates the project to the new version, including all files that reference the
        version. If a new version is not provided, will remove the -SNAPSHOT suffix
        from the current version.
    """)

val project = Framework.project ?: error("unable to find project")

// Get current version
if (version == null) {
    val current = project.version ?: error("unable to determine the current project version")
    if (current.endsWith("-SNAPSHOT")) {
        version = current.removeSuffix("-SNAPSHOT")
        echo("Changing the project version to $version")
    }
}

if (version == null)
    error("the new version must be specified")

// BUG: the Kotlin compiler is not recognizing version as String after null check
val newVersion = version!!

val versionRegex = Regex("""[\d.]*(-SNAPSHOT)?""")
if (!newVersion.matches(versionRegex))
    error("invalid version does not match regex '${versionRegex.pattern}'")

// Update gradle build
project.file.replace(Regex("""^version\s*=\s*"(.*)""""), 1, newVersion)

// Update scripts
runScript("upgradeDependency", newVersion)

// Update readme
if (!newVersion.endsWith("-SNAPSHOT")) {
    val readme = File("README.md")
    readme.replace(Regex("""kotlin-scripting:([\d.]*)"""), 1, newVersion)
    readme.replace(Regex("""tags/v([\d.]*)"""), 1, newVersion)
}

// Publish new version so scripts resolve
exec("./install.sh").fail()
