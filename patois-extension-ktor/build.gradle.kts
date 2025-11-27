import org.gradle.api.tasks.bundling.Jar

plugins {
    alias(libs.plugins.kotlinJvm)
    id("signing")
    id("maven-publish")
}

dependencies {
    api(project(":patois-core"))
    implementation(libs.ktor.server.core)
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenKotlin") {
            from(components["kotlin"])
        }
    }

    repositories {
        mavenLocal()
    }
}

tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
}
