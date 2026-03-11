plugins {
    kotlin("jvm") version "2.0.20"
    id("com.google.devtools.ksp") version "2.0.20-1.0.25"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
    application
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

ktlint {
    filter {
        exclude { it.file.path.contains("generated") }
    }
}
dependencies {
    implementation("net.sourceforge.plantuml:plantuml:1.2024.6")
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":processor"))
    ksp(project(":processor"))
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}
