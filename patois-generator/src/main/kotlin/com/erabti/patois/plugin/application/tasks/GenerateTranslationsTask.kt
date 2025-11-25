package com.erabti.patois.plugin.application.tasks

import com.erabti.patois.plugin.application.runners.GenerateTranslationRunner
import com.erabti.patois.plugin.models.PatoisPluginExtension
import com.erabti.patois.plugin.models.toConfig
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction

abstract class GenerateTranslationsTask : DefaultTask() {

    @TaskAction
    fun execute() {
        val extension = project.extensions.findByType(PatoisPluginExtension::class.java)!!
        val config = extension.toConfig()
        val outputDir = config.outputDir

        val runner = GenerateTranslationRunner(config)
        runner.run()

        project.extensions.getByType(SourceSetContainer::class.java).getByName("main").java.srcDir(outputDir)
    }
}