import org.gradle.api.tasks.bundling.Jar

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("signing")
    id("maven-publish")
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

publishing {
    repositories {
        mavenLocal()
    }
}

tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
}
