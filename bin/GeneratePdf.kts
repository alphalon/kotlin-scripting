// Script: GeneratePdf.kts
//
//DIR $KO_PROJECT
//CMD GeneratePdf Generates a PDF from the project README.md file

import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Executes the [commandLine], waiting for the process to finish.
 */
fun exec(commandLine: String, workingDir: File? = null): Int {
    val command = commandLine.split(" ").toTypedArray()

    val process = ProcessBuilder(*command)
        .directory(workingDir ?: File(System.getProperty("user.dir")))
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()

    process.waitFor(5, TimeUnit.MINUTES)

    return process.exitValue()
}

exec("""pandoc -V geometry:margin=1in README.md -o kotlin-scripting.pdf""")
