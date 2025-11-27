package io.github.erabti.patois.plugin.application.parsers

import io.github.erabti.patois.plugin.models.TranslationNode

interface InputParser {
    fun read(input: String): List<TranslationNode>
}