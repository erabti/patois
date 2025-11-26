package com.erabti.patois.plugin.models

import com.erabti.patois.models.PatoisConfig
import java.io.File

fun PatoisPluginExtension.toConfig(): PatoisConfig {
    return PatoisConfig(
        className = className.get(),
        packageName = packageName.get(),
        inputDir = inputDir.asFile.get().absolutePath,
        outputDir = outputDir.asFile.get().absolutePath,
        argumentPattern = PatoisConfig.ArgumentPattern.fromString(argumentPattern.get()),
        baseLocale = baseLocale.get().ifBlank { null },
        enumName = enumName.get(),
        resolverName = resolverName.get(),
    )
}

val PatoisConfig.inputDirFile get() = File(inputDir)
val PatoisConfig.outputDirFile get() = File(outputDir)