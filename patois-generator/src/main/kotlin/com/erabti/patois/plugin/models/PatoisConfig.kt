package com.erabti.patois.plugin.models

import java.io.File


data class PatoisConfig(
    val className: String,
    val inputDir: File,
    val outputDir: File,
    val argumentPattern: ArgumentPattern = ArgumentPattern.CURLY_BRACES,
    val baseLocale: String?,
) {
    enum class ArgumentPattern {
        CURLY_BRACES, PRINTF_STYLE;

        companion object {
            fun fromString(value: String): ArgumentPattern {
                return when (value.uppercase()) {
                    "CURLY_BRACES" -> CURLY_BRACES
                    "PRINTF_STYLE" -> PRINTF_STYLE
                    else -> throw IllegalArgumentException("Unknown ArgumentPattern: $value")
                }
            }
        }
    }
}

fun PatoisPluginExtension.toConfig(): PatoisConfig {
    return PatoisConfig(
        className = className.get(),
        inputDir = inputDir.asFile.get(),
        outputDir = outputDir.asFile.get(),
        argumentPattern = PatoisConfig.ArgumentPattern.fromString(argumentPattern.get()),
        baseLocale = baseLocale.get().ifBlank { null },
    )
}