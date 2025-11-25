package com.erabti.patois.plugin.application.tasks

import com.erabti.patois.plugin.application.runners.GenerateTranslationRunner
import com.erabti.patois.plugin.models.PatoisPluginExtension
import com.erabti.patois.plugin.models.toConfig
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class GenerateTranslationsTask : DefaultTask() {

    @TaskAction
    fun execute() {
        val extension = project.extensions.findByType(PatoisPluginExtension::class.java)!!
        val config = extension.toConfig()

        val runner = GenerateTranslationRunner(config)
        runner.run()
    }
}