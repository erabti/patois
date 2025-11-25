package com.erabti.patois.plugin.readers

import com.erabti.patois.plugin.models.TranslationNode

interface InputReader {
    fun read(input: String): List<TranslationNode>
}