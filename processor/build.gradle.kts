plugins {
    kotlin("jvm") version "2.0.20"
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.google.devtools.ksp:symbol-processing-api:2.0.20-1.0.25")
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}