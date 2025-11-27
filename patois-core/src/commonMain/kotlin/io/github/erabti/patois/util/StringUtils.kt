package io.github.erabti.patois.util

fun String.toPascalCase(): String {
    return replaceFirstChar { it.uppercase() }
}