package io.github.erabti.patois.plugin.application.tasks

import io.github.erabti.patois.plugin.application.runners.GenerateTranslationRunner
import io.github.erabti.patois.plugin.models.PatoisPluginExtension
import io.github.erabti.patois.plugin.models.toConfig
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class GenerateTranslationsTask : DefaultTask() {

    @TaskAction
    fun execute() {
        val extension = project.extensions.findByType(PatoisPluginExtension::class.java)!!
        val config = extension.toConfig()

        val runner = GenerateTranslationRunner(
            config = config,
            logger = { message -> logger.lifecycle(message) }
        )
        runner.run()
    }
}