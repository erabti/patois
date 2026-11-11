package io.github.erabti.patois.plugin.application.runners

import io.github.erabti.patois.plugin.application.parsers.InputParser
import io.github.erabti.patois.plugin.models.TranslationNode
import java.io.File

private val reservedRootKeys = setOf("locale")

internal fun InputParser.readFile(file: File, content: String): List<TranslationNode> {
    val nodes = try {
        read(content)
    } catch (e: Exception) {
        throw IllegalStateException("Failed to parse translation file ${file.absolutePath}: ${e.message}", e)
    }

    val reserved = nodes.firstOrNull { it.key in reservedRootKeys }
    if (reserved != null) {
        throw IllegalStateException(
            "'${reserved.key}' can't be a top-level key in ${file.absolutePath}. " +
                "Every generated strings class already has a '${reserved.key}' property. Rename the key."
        )
    }

    return nodes
}
