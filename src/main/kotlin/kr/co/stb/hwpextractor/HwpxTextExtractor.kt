package kr.co.stb.hwpextractor

import kr.dogfoot.hwpxlib.`object`.HWPXFile
import kr.dogfoot.hwpxlib.`object`.content.section_xml.paragraph.Run
import kr.dogfoot.hwpxlib.reader.HWPXReader
import kr.dogfoot.hwpxlib.tool.textextractor.TextExtractor
import kr.dogfoot.hwpxlib.tool.textextractor.TextExtractMethod
import kr.dogfoot.hwpxlib.tool.textextractor.TextMarks
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile

class HwpxTextExtractor(
    private val debug: Boolean = false,
    private val extractMeta: Boolean = false,
    private val extractFiles: Boolean = false,
    private val outputDirectory: String? = null,
    private val password: String? = null
) {
    fun extract(file: File) {
        if (debug) {
            println("Extracting HWPX file: ${file.absolutePath}")
        }

        val hwpxFile = HWPXReader.fromFilepath(file.absolutePath)

        if (extractMeta) {
            extractMetadata(hwpxFile)
        }

        val text = extractText(hwpxFile)

        if (outputDirectory != null) {
            saveToFile(file, text)
        } else {
            println(text)
        }

        if (extractFiles) {
            extractEmbeddedFiles(file)
        }
    }

    private fun extractText(hwpxFile: HWPXFile): String {
        val result = StringBuilder()

        // hwpxlib의 TextExtractor 사용
        val text = TextExtractor.extract(
            hwpxFile,
            TextExtractMethod.InsertControlTextBetweenParagraphText,
            true,
            TextMarks()
        )
        result.append(text)

        return result.toString()
    }

    private fun extractMetadata(hwpxFile: HWPXFile) {
        println("=== Metadata ===")

        // Note: The hwpxlib API may not expose metadata directly
        // HWPX files are ZIP archives with XML content
        try {
            println("Metadata extraction from HWPX: Not fully supported by library")
            // Metadata would need to be extracted from XML files within the HWPX archive
        } catch (e: Exception) {
            println("Error reading metadata: ${e.message}")
        }
        println("===============\n")
    }

    private fun extractEmbeddedFiles(sourceFile: File) {
        if (debug) {
            println("Extracting embedded files from HWPX...")
        }

        try {
            ZipFile(sourceFile).use { zip ->
                val binDataEntries = zip.entries().asSequence()
                    .filter { it.name.startsWith("BinData/") && !it.isDirectory }
                    .toList()

                if (binDataEntries.isNotEmpty()) {
                    val outputDir = File(outputDirectory ?: sourceFile.parent,
                        "${sourceFile.nameWithoutExtension}_files")
                    outputDir.mkdirs()

                    var extractedCount = 0
                    binDataEntries.forEach { entry ->
                        val fileName = entry.name.substringAfterLast("/")
                        if (fileName.isNotEmpty()) {
                            val outputFile = File(outputDir, fileName)

                            try {
                                zip.getInputStream(entry).use { input ->
                                    FileOutputStream(outputFile).use { output ->
                                        input.copyTo(output)
                                    }
                                }
                                extractedCount++
                                if (debug) {
                                    println("Extracted: $fileName")
                                }
                            } catch (e: Exception) {
                                System.err.println("Failed to extract $fileName: ${e.message}")
                            }
                        }
                    }

                    if (extractedCount > 0) {
                        println("Extracted $extractedCount file(s) to: ${outputDir.absolutePath}")
                    }
                }
            }
        } catch (e: Exception) {
            System.err.println("Error extracting embedded files: ${e.message}")
            if (debug) {
                e.printStackTrace()
            }
        }
    }

    private fun saveToFile(sourceFile: File, text: String) {
        val outputDir = File(outputDirectory!!)
        outputDir.mkdirs()

        val outputFile = File(outputDir, "${sourceFile.nameWithoutExtension}.txt")
        outputFile.writeText(text, Charsets.UTF_8)

        if (debug) {
            println("Text saved to: ${outputFile.absolutePath}")
        }
    }
}
