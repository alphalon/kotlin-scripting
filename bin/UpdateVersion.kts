/*
 * Project: kotlin-scripting
 * Script:  UpdateVersion.kts
 */

//DIR $KO_PROJECT
//CMD updateVersion - Updates the project to a new version
//HELP

//COMPILER_OPTS -jvm-target 1.8
//DEPS io.alphalon.kotlin:kotlin-scripting:0.1.4

import io.alphalon.kotlin.scripting.*
import java.io.File

// Process arguments
fun List<String>.hasFlag(vararg options: String) = intersect(options.toList()).isNotEmpty()

val options = args.filter { it.startsWith("-") }.map(String::toLowerCase)
val arguments = args.filter { !it.startsWith("-") }

val printHelp = options.hasFlag("-h", "--help")
val force = options.hasFlag("-f", "--force")
var version = arguments.firstOrNull()

// Print usage and exit
if (printHelp)
    echoUsage("""
        Usage:
          updateVersion [version]

        Updates the project to the new version, including all files that reference the
        version. If a new version is not provided, will remove the -SNAPSHOT suffix
        from the current version.

        If run against a repository with changes, the newly changed files will not be
        committed.

        Options:
          -f, --force    Allow the version change even when the repo is dirty
    """)

val repo: GitRepository = currentRepo()
val project: GradleProject = currentProject()

// Make sure there are no changed files
val dirty = repo.isDirty()
if (dirty && !force)
    error("Cannot change the version with changed files in the repository")

val snapshotSuffix = "-SNAPSHOT"
fun String.isSnapshot() = endsWith(snapshotSuffix)

// Get current version
if (version == null) {
    val current = project.version ?: error("Unable to determine the current project version")
    if (current.isSnapshot()) {
        version = current.removeSuffix(snapshotSuffix)
        echo("Changing the project version to $version")
    }
}

if (version == null)
    error("The new version must be specified")

// BUG: the Kotlin compiler is not recognizing version as String after null check
val newVersion = version!!

val versionRegex = Regex("""[\d.]*(-SNAPSHOT)?""")
if (!newVersion.matches(versionRegex))
    error("Invalid version does not match regex '${versionRegex.pattern}'")

// Update Gradle project file
project.file.replace(Regex("""^version\s*=\s*"(.*)""""), 1, newVersion)

// Update all Kotlin scripts in project
runScript("upgradeDependency", newVersion).fail()

// Update framework version script
val script = File("scripts/ko-framework-version.sh")
script.replace(Regex(""".*KO_VERSION\s*=\s*"(.*)""""), 1, newVersion)

// Update version references in readme file
if (!newVersion.endsWith("-SNAPSHOT")) {
    val readme = File("README.md")
    readme.replace(Regex("""kotlin-scripting:([\d.]*)"""), 1, newVersion)
    readme.replace(Regex("""tags/v([\d.]*)"""), 1, newVersion)
}

// Commit these changes to repo
if (!dirty) {
    val message = if (newVersion.isSnapshot()) "Preparing for ${newVersion.removeSuffix(snapshotSuffix)}" else "Releasing v$newVersion"
    repo.changedFiles().forEach { repo.add(it) }
    repo.commit(message)
}

// Publish new version so scripts resolve
project.exec("publishToMavenLocal").fail()
