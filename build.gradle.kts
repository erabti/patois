import org.gradle.plugins.signing.SigningExtension

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
        val signingKey = findProperty("signingKey") as? String
        val signingPassword = findProperty("signingPassword") as? String
        
        if (signingKey.isNullOrBlank()) {
            logger.warn("⚠️  [${project.name}] 'signingKey' property is MISSING or EMPTY. In-memory signing will NOT be configured.")
        } else if (signingPassword.isNullOrBlank()) {
            logger.warn("⚠️  [${project.name}] 'signingKey' found but 'signingPassword' is MISSING or EMPTY.")
        } else {
            logger.lifecycle("✅ [${project.name}] Signing configuration: key found (length: ${signingKey.length}), password found (length: ${signingPassword.length}).")
            configure<SigningExtension> {
                useInMemoryPgpKeys(signingKey, signingPassword)
            }
        }

        extensions.configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
            configureBasedOnAppliedPlugins()
            pomFromGradleProperties()
            publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
            signAllPublications()
        }
    }
}
