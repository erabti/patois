package com.erabti.patois.plugin.models

import java.io.File


data class PatoisConfig(
    val className: String,
    val packageName: String,
    val inputDir: File,
    val outputDir: File,
    val argumentPattern: ArgumentPattern = ArgumentPattern.CURLY_BRACES,
    val baseLocale: String?,
    val enumName: String,
) {
    enum class ArgumentPattern {
        CURLY_BRACES, PRINTF_STYLE;

        internal fun wrapWithPattern(argumentName: String): String {
            return when (this) {
                CURLY_BRACES -> "{$argumentName}"
                PRINTF_STYLE -> "%$argumentName"
            }
        }

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
        packageName = packageName.get(),
        inputDir = inputDir.asFile.get(),
        outputDir = outputDir.asFile.get(),
        argumentPattern = PatoisConfig.ArgumentPattern.fromString(argumentPattern.get()),
        baseLocale = baseLocale.get().ifBlank { null },
        enumName = enumName.get(),
    )
}