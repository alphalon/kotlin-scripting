/*
 * Project: kotlin-scripting
 * Script:  pdf.kts
 */

//DIR  $KO_PROJECT
//CMD  pdf Generates a PDF from the project README.md file

import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Executes the [block] on a non-zero exit code and terminates the process.
 */
fun Process.fail(block: (() -> Unit)? = null) {
    if (exitValue() > 0) {
        block?.invoke()
        System.exit(exitValue())
    }
}

/**
 * Executes the [command], waiting for the process to finish.
 */
fun exec(command: List<String>, workingDir: File? = null): Process =
    ProcessBuilder(command).apply {
        workingDir?.let { directory(it) }
        redirectOutput(ProcessBuilder.Redirect.INHERIT)
        redirectError(ProcessBuilder.Redirect.INHERIT)
    }.start().apply {
        waitFor(5, TimeUnit.MINUTES)
    }

/**
 * Executes the [commandLine], performing a terribly naive parsing operation
 * to separate the arguments to pass to the process. Use at your own risk!
 */
fun exec(commandLine: String) = exec(commandLine.split(" "))

// Use pandoc to generate the PDF file
exec("pandoc -V geometry:margin=1in -V fontsize:12pt README.md -o kotlin-scripting.pdf").fail()
