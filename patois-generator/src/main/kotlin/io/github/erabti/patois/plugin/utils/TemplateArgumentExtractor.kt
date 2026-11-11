package io.github.erabti.patois.plugin.utils

import io.github.erabti.patois.models.PatoisConfig.ArgumentPattern
import io.github.erabti.patois.plugin.models.TemplateArgument

class TemplateArgumentExtractor(
    private val pattern: ArgumentPattern = ArgumentPattern.CURLY_BRACES,
) {
    fun extract(template: String): List<TemplateArgument> {
        return when (pattern) {
            ArgumentPattern.CURLY_BRACES -> extractCurlyBraces(template)
            ArgumentPattern.PRINTF_STYLE -> extractPrintfStyle(template)
        }
    }

    private fun extractCurlyBraces(template: String): List<TemplateArgument> {
        val regex = Regex("""\{(\w+)\}""")
        return regex.findAll(template)
            .map { match -> match.groupValues[1] }
            .distinct()
            .mapIndexed { index, name ->
                TemplateArgument(
                    name = name,
                    index = index
                )
            }
            .toList()
    }

    private fun extractPrintfStyle(template: String): List<TemplateArgument> {
        val regex = Regex("""%((\d+)\$)?([sd])""")
        val matches = regex.findAll(template).toList()

        return matches.mapIndexed { index, match ->
            val position = match.groupValues[2].toIntOrNull() ?: (index + 1)
            "arg$position"
        }.distinct().mapIndexed { index, name ->
            TemplateArgument(
                name = name,
                index = index
            )
        }
    }

}

