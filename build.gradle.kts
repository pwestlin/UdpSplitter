plugins {
    kotlin("jvm") version "2.3.21"
    application
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    idea
}

group = "nu.westlin"
version = "0.0.1-SNAPSHOT"
description = "UdpSplitter"

kotlin {
    jvmToolchain(25)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.3")
    // För att kunna testa asynkron kod enklare (valfritt men bra)
    testImplementation("org.awaitility:awaitility-kotlin:4.3.0")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

application {
    // Namnet på klassen som genereras av Kotlin.
    // Om filen heter Splitter.kt så heter klassen SplitterKt.
    mainClass.set("nu.westlin.udpsplitter.UdpSplitterKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "nu.westlin.udpsplitter.UdpSplitterKt"
    }

    // Detta gör JAR-filen "fet" genom att inkludera alla bibliotek, vilket gör den enkel att flytta mellan mappar.
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

/**
 * Säger åt IntelliJ att ladda ned javadoc och källkod.
 */
idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

/*
Om man får problem med en viss regel på bara ett eller kanske två ställen kan man slå av dem i koden mha ex.:
@Suppress("ktlint:standard:function-signature")
 */
configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    verbose.set(true)
}
