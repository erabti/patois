import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinNativeCompile

private val publishableModules = setOf(
    "patois-core",
    "patois-generator",
    "patois-extension-ktor",
)

plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.mavenPublish)
}

allprojects {
    group = "io.github.erabti.patois"
    version = "0.1.2"

    repositories {
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

subprojects {
    if (name !in publishableModules) {
        logger.lifecycle("Skipping Maven Central publication setup for $name (module not in publishableModules).")
        return@subprojects
    }

    // Dokka is needed so javadoc JARs are generated for Central
    pluginManager.apply("org.jetbrains.dokka")
    pluginManager.apply("com.vanniktech.maven.publish.base")
    plugins.withId("com.vanniktech.maven.publish.base") {
        val signingKey = findProperty("signingKey") as? String
        val signingPassword = findProperty("signingPassword") as? String

        if (signingKey.isNullOrBlank()) {
            logger.warn("[${project.name}] 'signingKey' property is MISSING or EMPTY. In-memory signing will NOT be configured.")
        } else if (signingPassword.isNullOrBlank()) {
            logger.warn("[${project.name}] 'signingKey' found but 'signingPassword' is MISSING or EMPTY.")
        } else {
            logger.lifecycle("[${project.name}] Signing configuration: key found (length: ${signingKey.length}), password found (length: ${signingPassword.length}).")
        }

        afterEvaluate {
            extensions.configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
                configureBasedOnAppliedPlugins()
                pomFromGradleProperties()
                publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
                signAllPublications()
            }
        }

        plugins.withId("signing") {
            if (!signingKey.isNullOrBlank() && !signingPassword.isNullOrBlank()) {
                configure<SigningExtension> {
                    useInMemoryPgpKeys(signingKey, signingPassword)
                }
            }
            
            // Fix for missing klib file during signing and metadata generation: ensure these tasks run after native compilation
            val nativeCompileTasks = tasks.withType<AbstractKotlinNativeCompile<*, *>>()
            tasks.withType<Sign>().configureEach {
                mustRunAfter(nativeCompileTasks)
            }
            tasks.withType<GenerateModuleMetadata>().configureEach {
                mustRunAfter(nativeCompileTasks)
            }
        }
    }
}
