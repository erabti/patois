package com.erabti.patois.plugin.readers

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlNode
import com.erabti.patois.plugin.models.TranslationNode

internal class YamlInputReader(private val yamlParser: Yaml = Yaml.default) : InputReader {
    override fun read(input: String): List<TranslationNode> {
        val yamlNode = yamlParser.parseToYamlNode(input)

        return TODO()
    }


    private fun parseYamlNode(node: YamlNode): TranslationNode {

    }
}