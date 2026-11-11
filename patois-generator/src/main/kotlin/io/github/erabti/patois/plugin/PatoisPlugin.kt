@file:Suppress("unused")

package io.github.erabti.patois.plugin

import io.github.erabti.patois.models.PatoisConfig
import io.github.erabti.patois.plugin.application.tasks.GenerateTranslationsTask
import io.github.erabti.patois.plugin.models.PatoisPluginExtension
import io.github.erabti.patois.plugin.utils.Constants
import io.github.erabti.patois.plugin.utils.PackageDetector
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.provider.Provider
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
        }

        val generateTask = project.tasks.register("generateTranslations", GenerateTranslationsTask::class.java) {
            it.group = "patois"
            it.description = "Generates translation files"

            it.className.set(extension.className)
            it.packageName.set(extension.packageName)
            it.inputDir.set(extension.inputDir)
            it.inputFiles.from(extension.inputDir.map { dir ->
                dir.asFileTree.matching { pattern -> pattern.include("*.yaml", "*.yml") }
            })
            it.outputDir.set(extension.outputDir)
            it.argumentPattern.set(extension.argumentPattern)
            it.baseLocale.set(extension.baseLocale)
            it.enumName.set(extension.enumName)
            it.resolverName.set(extension.resolverName)
            it.hasKtorExtension.set(extension.hasKtorExtension)
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
        extension.hasKtorExtension.convention(hasPatoisKtorExtension(project))
    }

    private fun hasPatoisKtorExtension(project: Project): Provider<Boolean> {
        return project.configurations.named("compileClasspath").flatMap { classpath ->
            classpath.incoming.resolutionResult.rootComponent.map { root -> root.dependsOnKtorExtension() }
        }
    }

    private fun ResolvedComponentResult.dependsOnKtorExtension(): Boolean {
        val seen = mutableSetOf<ComponentIdentifier>()
        val queue = ArrayDeque(listOf(this))
        while (queue.isNotEmpty()) {
            val component = queue.removeFirst()
            if (!seen.add(component.id)) continue
            val module = component.moduleVersion
            if (module?.group == "io.github.erabti.patois" && module.name == "patois-extension-ktor") return true
            component.dependencies.filterIsInstance<ResolvedDependencyResult>().forEach { queue.add(it.selected) }
        }
        return false
    }
}
