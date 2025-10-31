plugins {
    kotlin("jvm") version "1.9.22"
    application
}

group = "kr.co.stb"
version = "1.0.0"

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("kr.dogfoot:hwplib:1.1.4")
    implementation("kr.dogfoot:hwpxlib:1.0.5")
    implementation("commons-cli:commons-cli:1.5.0")
    implementation("com.formdev:flatlaf:3.2.5")

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("kr.co.stb.hwpextractor.MainKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "kr.co.stb.hwpextractor.MainKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

// Task to download launch4j
tasks.register("downloadLaunch4j") {
    group = "distribution"
    description = "Download launch4j (requires manual download)"

    doLast {
        val launch4jDir = file("${buildDir}/tools/launch4j")

        if (!launch4jDir.exists()) {
            println("")
            println("=" .repeat(60))
            println("launch4j가 필요합니다!")
            println("=" .repeat(60))
            println("")
            println("다운로드 방법:")
            println("1. https://sourceforge.net/projects/launch4j/files/launch4j-3/3.50/ 방문")
            println("2. launch4j-3.50-win32.zip 다운로드")
            println("3. build/tools/ 폴더에 압축 해제")
            println("")
            println("또는 간단하게:")
            println("- hwp-extractor.bat 을 사용하세요!")
            println("=" .repeat(60))
        } else {
            println("✓ launch4j found at ${launch4jDir.absolutePath}")
        }
    }
}

// Task to create Windows distribution package
tasks.register("createWindowsExe") {
    dependsOn("jar", "downloadLaunch4j")
    group = "distribution"
    description = "Creates Windows distribution package with real EXE"

    doLast {
        val exeDir = file("${buildDir}/exe")
        exeDir.mkdirs()

        // Copy JAR to exe directory
        val jarFile = file("${buildDir}/libs/${project.name}-${version}.jar")
        jarFile.copyTo(file("${exeDir}/hwp-extractor.jar"), overwrite = true)

        // Create batch fileㄹ launcher (GUI mode)
        val batContent = """
            @echo off
            start javaw -jar "%~dp0hwp-extractor.jar" %*
        """.trimIndent()
        file("${exeDir}/hwp-extractor.bat").writeText(batContent)

        // Create console batch file (CLI mode)
        val batConsoleContent = """
            @echo off
            java -jar "%~dp0hwp-extractor.jar" %*
        """.trimIndent()
        file("${exeDir}/hwp-extractor-cli.bat").writeText(batConsoleContent)

        // Copy VBS launcher
        //file("hwp-extractor.vbs").copyTo(file("${exeDir}/hwp-extractor.vbs"), overwrite = true)

        // Create README
        val readmeContent = """
            HWP Extractor v${version}
            ========================

            실행 방법:
            1. hwp-extractor.bat - 더블클릭으로 GUI 실행
            2. hwp-extractor.vbs - 더블클릭으로 GUI 실행 (창 없이)
            3. hwp-extractor-cli.bat - 명령줄에서 실행

            요구사항:
            - Java 11 이상 설치 필요

            사용 예:
            hwp-extractor-cli.bat sample.hwp
            hwp-extractor-cli.bat --console sample.hwp
            hwp-extractor-cli.bat --help
        """.trimIndent()
        file("${exeDir}/README.txt").writeText(readmeContent)

        // Create EXE using launch4j
        println("\nCreating EXE file using launch4j...")

        val launch4jExe = file("${buildDir}/tools/launch4j/launch4jc.exe")
        val configFile = file("launch4j-config.xml")

        if (launch4jExe.exists()) {
            // Update config file with absolute paths
            val configContent = configFile.readText()
                .replace("build/libs/hwp-extractor-1.0.0.jar", jarFile.absolutePath)
                .replace("build/exe/hwp-extractor.exe", file("${exeDir}/hwp-extractor.exe").absolutePath)

            val tempConfig = file("${buildDir}/launch4j-config-temp.xml")
            tempConfig.writeText(configContent)

            // Execute launch4j
            val process = ProcessBuilder()
                .command(launch4jExe.absolutePath, tempConfig.absolutePath)
                .redirectErrorStream(true)
                .start()

            process.inputStream.bufferedReader().use { reader ->
                reader.lines().forEach { line -> println(line) }
            }

            val exitCode = process.waitFor()

            if (exitCode == 0) {
                println("✓ EXE file created successfully!")
            } else {
                println("⚠ Failed to create EXE (exit code: $exitCode)")
            }
        } else {
            println("⚠ launch4j not found, skipping EXE creation")
        }

        println("")
        println("=" .repeat(60))
        println("Windows 배포 패키지 생성 완료!")
        println("=" .repeat(60))
        println("위치: ${exeDir.absolutePath}")
        println("")
        println("포함 파일:")
        println("  - hwp-extractor.exe         (실행 파일)")
        println("  - hwp-extractor.jar         (메인 프로그램)")
        println("  - hwp-extractor.bat         (GUI 실행 파일)")
        println("  - hwp-extractor-cli.bat     (CLI 실행 파일)")
        println("  - README.txt                (사용 설명서)")
        println("=" .repeat(60))
    }
}
