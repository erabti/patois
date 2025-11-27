import org.gradle.api.tasks.bundling.Jar

plugins {
    alias(libs.plugins.kotlinJvm)
    `java-gradle-plugin`
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(gradleApi())
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kaml)
    implementation(project(":patois-core"))

    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

java {
    withSourcesJar()
}

gradlePlugin {
    plugins {
        create("patoisPlugin") {
            id = "io.github.erabti.patois"
            implementationClass = "io.github.erabti.patois.plugin.PatoisPlugin"
            displayName = "Patois i18n Code Generator"
            description = "Gradle plugin for generating type-safe Kotlin translations from files"
        }
    }
}



tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
}
