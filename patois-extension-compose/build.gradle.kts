plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    sourceSets {
        commonMain.dependencies {
            implementation(project(":patois-core"))
            implementation(libs.compose.runtime)
        }
    }
}