@file:Suppress("unused")

package io.github.erabti.patois.plugin

import io.github.erabti.patois.models.PatoisConfig
import io.github.erabti.patois.plugin.application.tasks.GenerateTranslationsTask
import io.github.erabti.patois.plugin.models.PatoisPluginExtension
import io.github.erabti.patois.plugin.utils.Constants
import io.github.erabti.patois.plugin.utils.PackageDetector
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.SourceSetContainer

/**
 * Gradle plugin for generating type-safe Kotlin translation classes from YAML/JSON files.
 *
 * Apply this plugin to your project and configure it using the `patois` extension:
 *
 * ```kotlin
 * plugins {
 *     id("io.github.erabti.patois")
 * }
 *
 * patois {
 *     className.set("AppStrings")
 *     packageName.set("com.example.i18n")
 *     inputDir.set(file("src/main/resources/i18n"))
 * }
 * ```
 *
 * The plugin registers a `generateTranslations` task that runs before `compileKotlin`.
 */
class PatoisPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create(
            "patois", PatoisPluginExtension::class.java
        )

        setupDefaults(project, extension)

        project.afterEvaluate {
            val outputDir = extension.outputDir.asFile.get()
            project.extensions.getByType(SourceSetContainer::class.java).getByName("main").java.srcDir(outputDir)

            val hasKtorExtension = hasPatoisKtorExtension(project)
            extension.hasKtorExtension.set(hasKtorExtension)
            project.extensions.extraProperties.set("patoisHasKtorExtension", hasKtorExtension)
        }

        val generateTask = project.tasks.register("generateTranslations", GenerateTranslationsTask::class.java) {
            it.group = "patois"
            it.description = "Generates translation files"
        }

        project.tasks.named("compileKotlin").configure {
            it.dependsOn(generateTask)
        }
    }


    private fun setupDefaults(project: Project, extension: PatoisPluginExtension) {
        extension.className.convention(Constants.DEFAULT_CLASS_NAME)
        extension.inputDir.convention(project.layout.projectDirectory.dir(Constants.DEFAULT_INPUT_DIR))
        extension.outputDir.convention(project.layout.buildDirectory.dir(Constants.DEFAULT_OUTPUT_DIR))
        extension.argumentPattern.convention(PatoisConfig.ArgumentPattern.CURLY_BRACES.name)
        extension.baseLocale.convention("")
        val detectedPackage = PackageDetector.detectPackageFromSource(
            project.file("src/main/kotlin")
        ) ?: project.group.toString()

        extension.packageName.convention(detectedPackage)
        extension.enumName.convention(Constants.DEFAULT_ENUM_NAME)
        extension.resolverName.convention(Constants.DEFAULT_RESOLVER_NAME)
        extension.hasKtorExtension.convention(false)
    }

    private fun hasPatoisKtorExtension(project: Project): Boolean {
        return project.configurations.asSequence().any { configuration ->
            configuration.dependencies.any { dependency ->
                when (dependency) {
                    is ProjectDependency -> dependency.name == "patois-extension-ktor"
                    else -> dependency.group == "io.github.erabti.patois" && dependency.name == "patois-extension-ktor"
                }
            }
        }
    }
}
