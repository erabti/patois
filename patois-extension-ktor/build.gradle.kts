plugins {
    alias(libs.plugins.kotlinJvm)
    `java-library`
    alias(libs.plugins.dokka)
}

dependencies {
    api(project(":patois-core"))
    implementation(libs.ktor.server.core)
}
