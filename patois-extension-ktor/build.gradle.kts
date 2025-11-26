plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    api(project(":patois-core"))
    implementation(libs.ktor.server.core)
}