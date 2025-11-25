package com.erabti.patois.plugin.application.parsers

import com.erabti.patois.plugin.models.TranslationNode

interface InputParser {
    fun read(input: String): List<TranslationNode>
}