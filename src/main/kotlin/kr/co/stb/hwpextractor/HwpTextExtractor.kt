package kr.co.stb.hwpextractor

import kr.dogfoot.hwplib.`object`.HWPFile
import kr.dogfoot.hwplib.`object`.bodytext.Section
import kr.dogfoot.hwplib.`object`.bodytext.control.Control
import kr.dogfoot.hwplib.`object`.bodytext.control.ControlTable
import kr.dogfoot.hwplib.`object`.bodytext.paragraph.Paragraph
import kr.dogfoot.hwplib.`object`.bodytext.paragraph.text.ParaText
import kr.dogfoot.hwplib.reader.HWPReader
import kr.dogfoot.hwplib.tool.textextractor.TextExtractMethod
import kr.dogfoot.hwplib.tool.textextractor.TextExtractor
import java.io.File
import java.io.FileOutputStream

class HwpTextExtractor(
    private val debug: Boolean = false,
    private val extractMeta: Boolean = false,
    private val extractFiles: Boolean = false,
    private val outputDirectory: String? = null,
    private val password: String? = null
) {
    fun extract(file: File) {
        if (debug) {
            println("Extracting HWP file: ${file.absolutePath}")
        }

        val hwpFile = HWPReader.fromFile(file.absolutePath)

        if (extractMeta) {
            extractMetadata(hwpFile)
        }

        val text = extractText(hwpFile)

        if (outputDirectory != null) {
            saveToFile(file, text)
        } else {
            println(text)
        }

        if (extractFiles) {
            extractEmbeddedFiles(hwpFile, file)
        }
    }

    private fun extractText(hwpFile: HWPFile): String {
        val result = StringBuilder()

        // hwplib의 TextExtractor 사용
        val text = TextExtractor.extract(hwpFile, TextExtractMethod.InsertControlTextBetweenParagraphText)
        result.append(text)

        return result.toString()
    }

    private fun extractMetadata(hwpFile: HWPFile) {
        println("=== Metadata ===")

        val docInfo = hwpFile.docInfo
        if (docInfo != null) {
            val props = docInfo.documentProperties
            if (props != null) {
                // Note: The hwplib API may not expose all properties directly
                // Available properties may vary by version
                try {
                    println("Document Properties: Available")
                    // Add any accessible properties here when discovered
                } catch (e: Exception) {
                    println("Error reading metadata: ${e.message}")
                }
            }
        }
        println("===============\n")
    }

    private fun extractEmbeddedFiles(hwpFile: HWPFile, sourceFile: File) {
        if (debug) {
            println("Extracting embedded files...")
        }

        val binData = hwpFile.binData
        if (binData != null && binData.embeddedBinaryDataList != null) {
            val embeddedList = binData.embeddedBinaryDataList

            if (embeddedList.size > 0) {
                val outputDir = File(outputDirectory ?: sourceFile.parent,
                    "${sourceFile.nameWithoutExtension}_files")
                outputDir.mkdirs()

                for (i in 0 until embeddedList.size) {
                    val embedded = embeddedList[i]
                    val fileName = embedded.name ?: "embedded_$i"

                    val outputFile = File(outputDir, fileName)

                    try {
                        FileOutputStream(outputFile).use { fos ->
                            val data = embedded.data
                            if (data != null) {
                                fos.write(data)
                            }
                        }
                        if (debug) {
                            println("Extracted: $fileName")
                        }
                    } catch (e: Exception) {
                        System.err.println("Failed to extract $fileName: ${e.message}")
                    }
                }

                println("Extracted ${embeddedList.size} file(s) to: ${outputDir.absolutePath}")
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
