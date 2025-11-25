plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":patois-core"))
    implementation(libs.ktor.server.core)
}