plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    js(IR) {
        browser()
        nodejs()
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.stdlib)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}