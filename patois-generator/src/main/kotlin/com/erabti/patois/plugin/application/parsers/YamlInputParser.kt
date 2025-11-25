package com.erabti.patois.plugin.application.parsers

import com.charleskorn.kaml.*
import com.erabti.patois.plugin.models.TranslationNode
import com.erabti.patois.plugin.utils.TemplateArgumentExtractor

class YamlInputParser(
    private val yamlParser: Yaml = Yaml.default,
    private val argumentExtractor: TemplateArgumentExtractor = TemplateArgumentExtractor(),
) : InputParser {
    override fun read(input: String): List<TranslationNode> {
        val yamlNode = yamlParser.parseToYamlNode(input)

        return when (yamlNode) {
            is YamlMap -> yamlNode.entries.entries.map { (key, value) ->
                parseYamlNode(key.content, value)
            }

            else -> throw IllegalArgumentException("Root YAML element must be a map")
        }
    }

    private fun parseYamlNode(key: String, node: YamlNode): TranslationNode {
        return when (node) {
            is YamlScalar -> {
                val value = node.content
                val arguments = argumentExtractor.extract(value)
                TranslationNode.LeafNode(
                    key = key, value = value, arguments = arguments
                )
            }

            is YamlMap -> TranslationNode.MapNode(
                key = key, children = node.entries.entries.map { (childKey, childValue) ->
                    parseYamlNode(childKey.content, childValue)
                })

            is YamlList -> TranslationNode.ListNode(
                key = key, items = node.items.mapIndexed { index, item ->
                    parseYamlNode(index.toString(), item)
                })

            else -> throw IllegalArgumentException("Unsupported YAML node type: ${node::class}")
        }
    }
}
