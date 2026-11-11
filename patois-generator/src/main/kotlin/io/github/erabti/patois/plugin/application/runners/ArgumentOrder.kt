package io.github.erabti.patois.plugin.application.runners

import io.github.erabti.patois.plugin.models.TranslationNode

internal fun LocaleTranslation.withArgumentOrderOf(base: LocaleTranslation): LocaleTranslation =
    copy(nodes = nodes.withArgumentOrderOf(base.nodes))

private fun List<TranslationNode>.withArgumentOrderOf(base: List<TranslationNode>): List<TranslationNode> {
    val baseByKey = base.associateBy { it.key }
    return map { node -> baseByKey[node.key]?.let { node.withArgumentOrderOf(it) } ?: node }
}

private fun TranslationNode.withArgumentOrderOf(base: TranslationNode): TranslationNode = when {
    this is TranslationNode.MapNode && base is TranslationNode.MapNode ->
        TranslationNode.MapNode(key, children.withArgumentOrderOf(base.children))

    this is TranslationNode.LeafNode && base is TranslationNode.LeafNode -> {
        val baseNames = base.arguments.map { it.name }
        val sameArguments = arguments.map { it.name }.sorted() == baseNames.sorted()
        if (sameArguments) {
            TranslationNode.LeafNode(key, value, arguments.sortedBy { baseNames.indexOf(it.name) })
        } else {
            this
        }
    }

    else -> this
}
