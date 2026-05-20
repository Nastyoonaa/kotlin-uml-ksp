plugins {
    kotlin("jvm") version "2.0.20"
    id("com.google.devtools.ksp") version "2.0.20-1.0.25"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"

    id("com.vanniktech.maven.publish") version "0.29.0"
}

group = "io.github.nastyoonaa"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.sourceforge.plantuml:plantuml:1.2024.6")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.json:json:20240303")
    implementation(kotlin("stdlib"))
}

kotlin {
    jvmToolchain(17)
}

java {
    withSourcesJar()
    withJavadocJar()
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(
        "io.github.nastyoonaa",
        "kotlin-uml-ksp",
        "1.0.0"
    )

    pom {
        name.set("Kotlin UML Generator")
        description.set("KSP-based UML generator for Kotlin projects")
        url.set("https://github.com/Nastyoonaa/kotlin-uml-ksp")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set("nastyoonaa")
                name.set("Анастасия Ципенюк")
            }
        }

        scm {
            url.set("https://github.com/Nastyoonaa/kotlin-uml-ksp")
        }
    }
}
