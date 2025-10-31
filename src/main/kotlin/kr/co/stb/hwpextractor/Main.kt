package kr.co.stb.hwpextractor

import org.apache.commons.cli.*
import java.io.File
import kotlin.system.exitProcess

const val VERSION = "1.0.0"

fun main(args: Array<String>) {
    // Launch GUI mode if no arguments provided
    if (args.isEmpty()) {
        launchGui()
        return
    }

    val options = Options().apply {
        addOption("h", "help", false, "Show this help message and exit")
        addOption("d", "debug", false, "Enable debug mode")
        addOption("m", "extract-meta", false, "Extract metadata information")
        addOption("f", "extract-files", false, "Extract embedded files")
        addOption("o", "output-directory", true, "Output directory for extracted files")
        addOption("p", "password", true, "Password for encrypted files")
        addOption("c", "console", false, "Output text to console instead of file")
        addOption("v", "version", false, "Show version information")
        addOption("g", "gui", false, "Launch GUI mode")
    }

    val parser: CommandLineParser = DefaultParser()
    val formatter = HelpFormatter()

    try {
        val cmd = parser.parse(options, args)

        if (cmd.hasOption("help")) {
            printUsage(formatter, options)
            exitProcess(0)
        }

        if (cmd.hasOption("version")) {
            println("hwp-extractor version $VERSION")
            exitProcess(0)
        }

        if (cmd.hasOption("gui")) {
            launchGui()
            return
        }

        val targetFiles = cmd.argList
        if (targetFiles.isEmpty()) {
            System.err.println("hwp-extract: error: the following arguments are required: target_file")
            printUsage(formatter, options)
            exitProcess(1)
        }

        val debug = cmd.hasOption("debug")
        val extractMeta = cmd.hasOption("extract-meta")
        val extractFiles = cmd.hasOption("extract-files")
        val outputDir = cmd.getOptionValue("output-directory")
        val password = cmd.getOptionValue("password")
        val outputToConsole = cmd.hasOption("console")

        val extractor = HwpExtractor(
            debug = debug,
            extractMeta = extractMeta,
            extractFiles = extractFiles,
            outputDirectory = outputDir,
            password = password,
            outputToConsole = outputToConsole
        )

        var hasError = false
        for (filePath in targetFiles) {
            try {
                extractor.extract(filePath)
            } catch (e: Exception) {
                System.err.println("Error processing file '$filePath': ${e.message}")
                if (debug) {
                    e.printStackTrace()
                }
                hasError = true
            }
        }

        if (hasError) {
            exitProcess(1)
        }

    } catch (e: ParseException) {
        System.err.println("hwp-extract: error: ${e.message}")
        printUsage(formatter, options)
        exitProcess(1)
    }
}

fun printUsage(formatter: HelpFormatter, options: Options) {
    formatter.printHelp(
        "hwp-extract [-h] [--debug] [--extract-meta] [--extract-files] [--console] " +
                "[--output-directory OUTPUT_DIRECTORY] [--password PASSWORD] [--version] " +
                "target_file [target_file ...]",
        options
    )
}
