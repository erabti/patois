plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.compose) apply false
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

allprojects {
    group = "io.github.erabti.patois"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

subprojects {
    plugins.withId("maven-publish") {
        afterEvaluate {
            extensions.configure<org.gradle.api.publish.PublishingExtension> {
                publications.withType<org.gradle.api.publish.maven.MavenPublication>().configureEach {
                    val moduleDescription = when (project.path) {
                        ":patois-core" -> "Kotlin Multiplatform runtime contracts and utilities for Patois translations."
                        ":patois-extension-ktor" -> "Ktor server extension for resolving Patois translation bundles in routes."
                        ":patois-generator" -> "Gradle plugin that generates type-safe translation classes from i18n resources."
                        else -> "Patois module ${project.name}"
                    }

                    pom {
                        name.set(artifactId)
                        description.set(moduleDescription)
                        url.set("https://github.com/erabti/patois")

                        licenses {
                            license {
                                name.set("The Apache License, Version 2.0")
                                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                            }
                        }

                        developers {
                            developer {
                                id.set("erabti")
                                name.set("Erabti")
                                url.set("https://github.com/erabti")
                            }
                        }

                        scm {
                            url.set("https://github.com/erabti/patois")
                            connection.set("scm:git:https://github.com/erabti/patois.git")
                            developerConnection.set("scm:git:ssh://git@github.com/erabti/patois.git")
                        }
                    }

                    tasks.findByName("javadocJar")?.let { artifact(it) }
                }
            }
        }
    }

    plugins.withId("signing") {
        afterEvaluate {
            extensions.configure<org.gradle.plugins.signing.SigningExtension> {
                val signingKey = findProperty("signingKey") as String?
                val signingPassword = findProperty("signingPassword") as String?
                if (!signingKey.isNullOrBlank() && !signingPassword.isNullOrBlank()) {
                    useInMemoryPgpKeys(signingKey, signingPassword)
                    sign(extensions.getByType<org.gradle.api.publish.PublishingExtension>().publications)
                }
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(findProperty("sonatypeUsername") as String?)
            password.set(findProperty("sonatypePassword") as String?)
        }
    }
}
