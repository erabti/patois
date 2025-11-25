package com.erabti.patois.plugin

import com.erabti.patois.plugin.application.tasks.GenerateTranslationsTask
import com.erabti.patois.plugin.models.PatoisConfig
import com.erabti.patois.plugin.models.PatoisPluginExtension
import com.erabti.patois.plugin.utils.Constants
import com.erabti.patois.plugin.utils.PackageDetector
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer

class PatoisPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create(
            "patois", PatoisPluginExtension::class.java
        )
        setupDefaults(project, extension)

        project.afterEvaluate {
            val outputDir = extension.outputDir.asFile.get()
            project.extensions.getByType(SourceSetContainer::class.java)
                .getByName("main").java.srcDir(outputDir)
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
        ) ?: project.group.toString().ifEmpty { "" }
        
        extension.packageName.convention(detectedPackage)
    }
}
