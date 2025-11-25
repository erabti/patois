package com.erabti.patois.plugin.models

import java.io.File


data class PatoisConfig(
    val className: String,
    val inputDir: File,
    val outputDir: File,
)

fun PatoisPluginExtension.toConfig(): PatoisConfig {
    return PatoisConfig(
        className = className.get(),
        inputDir = inputDir.asFile.get(),
        outputDir = outputDir.asFile.get()
    )
}