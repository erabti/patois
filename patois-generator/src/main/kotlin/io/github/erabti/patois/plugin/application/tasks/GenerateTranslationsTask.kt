package io.github.erabti.patois.plugin.application.tasks

import io.github.erabti.patois.models.PatoisConfig
import io.github.erabti.patois.plugin.application.runners.GenerateTranslationRunner
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class GenerateTranslationsTask : DefaultTask() {

    @get:Input
    abstract val className: Property<String>

    @get:Input
    abstract val packageName: Property<String>

    @get:InputDirectory
    @get:Optional
    @get:IgnoreEmptyDirectories
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val argumentPattern: Property<String>

    @get:Input
    @get:Optional
    abstract val baseLocale: Property<String>

    @get:Input
    abstract val enumName: Property<String>

    @get:Input
    abstract val resolverName: Property<String>

    @get:Input
    abstract val hasKtorExtension: Property<Boolean>

    @TaskAction
    fun execute() {
        val inputDirFile = inputDir.asFile.orNull
        if (inputDirFile == null || !inputDirFile.exists()) {
            return
        }

        val config = PatoisConfig(
            className = className.get(),
            packageName = packageName.get(),
            inputDir = inputDirFile.absolutePath,
            outputDir = outputDir.asFile.get().absolutePath,
            argumentPattern = PatoisConfig.ArgumentPattern.fromString(argumentPattern.get()),
            baseLocale = baseLocale.orNull?.ifBlank { null },
            enumName = enumName.get(),
            resolverName = resolverName.get(),
            hasKtorExtension = hasKtorExtension.get(),
        )

        val runner = GenerateTranslationRunner(
            config = config,
            logger = { message -> logger.lifecycle(message) }
        )
        runner.run()
    }
}