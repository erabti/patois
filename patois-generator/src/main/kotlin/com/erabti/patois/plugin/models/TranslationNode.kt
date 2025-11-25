package com.erabti.patois.plugin.models


sealed class TranslationNode(
    val key: String,
) {
    class LeafNode(
        key: String,
        val value: String,
    ) : TranslationNode(key)

    class ParentNode(
        key: String,
        val children: List<TranslationNode>,
    ) : TranslationNode(key)
}
