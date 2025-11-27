plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.compose) apply false
    id("com.vanniktech.maven.publish.base") version "0.28.0"
}

allprojects {
    group = "io.github.erabti.patois"
    version = "0.1.0"

    repositories {
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

subprojects {
    pluginManager.apply("com.vanniktech.maven.publish.base")
    plugins.withId("com.vanniktech.maven.publish.base") {
        extensions.configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
            configureBasedOnAppliedPlugins()
            pomFromGradleProperties()
            publishToMavenCentral(automaticRelease = true)
            signAllPublications()
        }
    }
}
