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
