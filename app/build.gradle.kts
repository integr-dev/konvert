plugins {
    kotlin("jvm") version "2.0.20"
    id("com.google.devtools.ksp") version "2.0.20-1.0.25"
}

group = "net.integr"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))

    // KSP Compiler
    ksp("com.google.dagger:dagger-compiler:2.51.1") // Let KSP know about the KSP Compiler
    implementation("com.google.dagger:dagger-compiler:2.51.1") // Let the main module know about the KSP Compiler

    // Annotation Processor
    ksp(project(":processor")) // Let KSP know about the processor module
    implementation(project(":processor")) // Let the main module know about the processor module
}

kotlin {
    jvmToolchain(17)
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}