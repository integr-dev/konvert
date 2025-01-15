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

    ksp("com.google.dagger:dagger-compiler:2.51.1") // Let KSP know about the KSP Compiler
    implementation("com.google.dagger:dagger-compiler:2.51.1") // Let the main module know about the KSP Compiler
}

kotlin {
    jvmToolchain(17)
}