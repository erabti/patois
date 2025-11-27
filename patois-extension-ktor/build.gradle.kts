import org.gradle.api.tasks.bundling.Jar

plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    api(project(":patois-core"))
    implementation(libs.ktor.server.core)
}

java {
    withSourcesJar()
}



tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
}
