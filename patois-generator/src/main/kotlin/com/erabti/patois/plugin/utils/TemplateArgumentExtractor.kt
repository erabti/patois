package com.erabti.patois.plugin.utils

import com.erabti.patois.models.PatoisConfig.ArgumentPattern
import com.erabti.patois.plugin.models.TemplateArgument

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
            .mapIndexed { index, match ->
                TemplateArgument(
                    name = match.groupValues[1],
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
            val name = "arg$position"

            TemplateArgument(
                name = name,
                index = index
            )
        }
    }

}

