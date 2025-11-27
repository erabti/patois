package io.github.erabti.patois.plugin.utils

import java.io.File

object PackageDetector {
    fun detectPackageFromSource(sourceDir: File): String? {
        if (!sourceDir.exists()) return null
        
        return sourceDir.walk()
            .filter { it.extension == "kt" }
            .take(5)
            .mapNotNull { extractPackage(it) }
            .groupBy { it }
            .maxByOrNull { it.value.size }
            ?.key
    }
    
    private fun extractPackage(file: File): String? {
        return file.useLines { lines ->
            lines.firstOrNull { it.trim().startsWith("package ") }
                ?.removePrefix("package ")
                ?.trim()
                ?.removeSuffix(";")
        }
    }
}
