/*
 * Project: kotlin-scripting
 * Script:  pdf.kts
 */

//DIR  $KO_PROJECT
//CMD  pdf Generates a PDF from the project README.md file

import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Executes the [block] on a non-zero exit code and terminates the process.
 */
fun Process?.fail(block: (() -> Unit)? = null) {
    try {
        when {
            this == null -> System.exit(1)
            exitValue() > 0 -> {
                block?.invoke()
                System.exit(exitValue())
            }
        }
    } catch (e: IllegalThreadStateException) {
        println("ERROR: the process has not terminated")
        System.exit(1)
    }
}

/**
 * Executes the [command], waiting for the process to finish.
 */
fun exec(command: List<String>, workingDir: File? = null, waitForMinutes: Long = 60): Process? {
    val builder = ProcessBuilder(command).apply {
        workingDir?.let { directory(it) }
        redirectOutput(ProcessBuilder.Redirect.INHERIT)
        redirectError(ProcessBuilder.Redirect.INHERIT)
    }

    try {
        return builder.start().apply {
            try {
                waitFor(waitForMinutes, TimeUnit.MINUTES)
            } catch (e: InterruptedException) {
                println("WARNING: the process (${command.first()}) is taking longer than $waitForMinutes minutes")
            }
        }
    } catch (e: IOException) {
        println("ERROR: ${e.message}")
    }

    return null
}

/**
 * Executes the [commandLine], performing a terribly naive parsing operation
 * to separate the arguments to pass to the process. Use at your own risk!
 */
fun exec(commandLine: String) = exec(commandLine.split(" "))

// Use pandoc to generate the PDF file
exec("pandoc -V geometry:margin=1in -V fontsize:12pt README.md -o kotlin-scripting.pdf").fail()
