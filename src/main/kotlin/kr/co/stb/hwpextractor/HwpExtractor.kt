package kr.co.stb.hwpextractor

import java.io.File

class HwpExtractor(
    private val debug: Boolean = false,
    private val extractMeta: Boolean = false,
    private val extractFiles: Boolean = false,
    private val outputDirectory: String? = null,
    private val password: String? = null
) {
    fun extract(filePath: String) {
        val file = File(filePath)

        if (!file.exists()) {
            throw IllegalArgumentException("File not found: $filePath")
        }

        if (!file.isFile) {
            throw IllegalArgumentException("Not a file: $filePath")
        }

        val extension = file.extension.lowercase()

        if (debug) {
            println("Processing file: $filePath (extension: $extension)")
        }

        when (extension) {
            "hwp" -> {
                val extractor = HwpTextExtractor(debug, extractMeta, extractFiles, outputDirectory, password)
                extractor.extract(file)
            }
            "hwpx" -> {
                val extractor = HwpxTextExtractor(debug, extractMeta, extractFiles, outputDirectory, password)
                extractor.extract(file)
            }
            else -> {
                throw IllegalArgumentException("Unsupported file format: $extension (expected .hwp or .hwpx)")
            }
        }
    }
}
