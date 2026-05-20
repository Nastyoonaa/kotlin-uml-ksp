plugins {
    kotlin("jvm") version "2.0.20"
    id("com.google.devtools.ksp") version "2.0.20-1.0.25"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"

    `java-library`
    `maven-publish`
    signing
}

group = "io.github.nastyoonaa"
version = "1.0.0"

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
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.json:json:20240303")
    implementation(kotlin("stdlib-jdk8"))
}

kotlin {
    jvmToolchain(17)
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["java"])

            groupId = "io.github.nastyoonaa"
            artifactId = "kotlin-uml-ksp"
            version = "1.0.0"

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
                    connection.set("scm:git:git://github.com/Nastyoonaa/kotlin-uml-ksp.git")
                    developerConnection.set("scm:git:ssh://github.com/Nastyoonaa/kotlin-uml-ksp.git")
                    url.set("https://github.com/Nastyoonaa/kotlin-uml-ksp")
                }
            }
        }
    }

    repositories {
        maven {
            name = "central"
            url = uri("https://central.sonatype.com/api/v1/publisher")

            credentials {
                username = findProperty("mavenCentralUsername") as String?
                password = findProperty("mavenCentralPassword") as String?
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        findProperty("signingInMemoryKeyId") as String?,
        findProperty("signingInMemoryKey") as String?,
        findProperty("signingInMemoryKeyPassword") as String?
    )
    sign(publishing.publications["release"])
}
