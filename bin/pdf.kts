/*
 * Project: kotlin-scripting
 * Script:  pdf.kts
 */

//DIR  $KO_PROJECT
//CMD  pdf Generates a PDF from the project README.md file

//COMPILER_OPTS -jvm-target 1.8
//DEPS io.alphalon.kotlin:kotlin-scripting:0.1.3-SNAPSHOT

import io.alphalon.kotlin.scripting.*

// Use pandoc to generate the PDF file
exec("pandoc -V geometry:margin=1in -V fontsize:12pt README.md -o kotlin-scripting.pdf").fail()
exec("update-pdf")
