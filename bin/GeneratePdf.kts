// Project: kotlin-scripting
// Script:  GeneratePdf.kts
//
//DIR  $KO_PROJECT
//CMD  GeneratePdf Generates a PDF from the project README.md file

import java.io.File
import java.util.concurrent.TimeUnit

typealias ExitCode = Int

/**
 * Executes the block on a non-zero exit code and terminates the process.
 */
fun ExitCode.fail(block: (() -> Unit)? = null) {
    if (this > 0) {
        block?.invoke()
        System.exit(this)
    }
}

/**
 * Executes the [command], waiting for the process to finish.
 */
fun exec(command: List<String>, workingDir: File? = null): ExitCode = ProcessBuilder(command)
    .directory(workingDir ?: File(System.getProperty("user.dir")))
    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
    .redirectError(ProcessBuilder.Redirect.INHERIT)
    .start().run {
        waitFor(5, TimeUnit.MINUTES)
        exitValue()
    }

/**
 * Executes the [commandLine], performing a terribly naive parsing operation
 * to separate the arguments to pass to the process. Use at your own risk!
 */
fun exec(commandLine: String) = exec(commandLine.split(" "))

// Use pandoc to generate the PDF file
exec("pandoc -V geometry:margin=1in README.md -o kotlin-scripting.pdf").fail()
