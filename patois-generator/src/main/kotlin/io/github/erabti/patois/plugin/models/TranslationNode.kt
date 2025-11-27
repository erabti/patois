package io.github.erabti.patois.plugin.models


sealed class TranslationNode(
    val key: String,
) {
    class LeafNode(
        key: String,
        val value: String,
        val arguments: List<TemplateArgument> = emptyList(),
    ) : TranslationNode(key)

    class MapNode(
        key: String,
        val children: List<TranslationNode>,
    ) : TranslationNode(key)

    class ListNode(
        key: String,
        val items: List<TranslationNode>,
    ) : TranslationNode(key)


    fun prettyPrint(indent: String = ""): String {
        return when (this) {
            is LeafNode -> "${indent}LeafNode(key='$key', value='$value', arguments=$arguments)"
            is MapNode -> {
                val childrenStr = children.joinToString("\n") { it.prettyPrint("$indent  ") }
                "${indent}MapNode(key='$key') {\n$childrenStr\n$indent}"
            }

            is ListNode -> {
                val itemsStr = items.joinToString("\n") { it.prettyPrint("$indent  ") }
                "${indent}ListNode(key='$key') [\n$itemsStr\n$indent]"
            }
        }
    }

    override fun toString(): String {
        return prettyPrint()
    }
}
