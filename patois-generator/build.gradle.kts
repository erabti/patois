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
}

gradlePlugin {
    plugins {
        create("patoisPlugin") {
            id = "com.erabti.patois"
            implementationClass = "com.erabti.patois.plugin.PatoisPlugin"
            displayName = "Patois i18n Code Generator"
            description = "Gradle plugin for generating type-safe Kotlin translations from files"
        }
    }
}
