plugins {
    kotlin("jvm") version "2.0.20"

    `java-library`
    `maven-publish`
    signing
}

group = "io.github.nastyoonaa"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("com.google.devtools.ksp:symbol-processing-api:2.0.20-1.0.25")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("processor") {

            from(components["java"])

            groupId = "io.github.nastyoonaa"
            artifactId = "kotlin-uml-ksp-processor"
            version = "1.0.0"

            pom {
                name.set("Kotlin UML Generator - Processor")
                description.set("KSP processor for UML generation")
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
}

signing {
    useGpgCmd()
    sign(publishing.publications["processor"])
}