package com.erabti.patois.plugin.application.generators

import com.erabti.patois.plugin.models.TranslationNode
import com.erabti.patois.plugin.utils.Constants
import com.erabti.patois.util.toPascalCase
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec

internal class TranslationClassSpecBuilder(
    private val specFactory: TranslationSpecFactory = TranslationSpecFactory,
    private val typeSpec: TypeSpec.Companion = TypeSpec,
) {
    fun build(className: ClassName, nodes: List<TranslationNode>) =
        buildClass(className, nodes, Constants.BASE_APP_STRINGS_CLASS_NAME)

    private fun buildClass(
        className: ClassName,
        nodes: List<TranslationNode>,
        extends: ClassName? = null,
        isAbstract: Boolean = true,
        isPrivate: Boolean = false,
    ) = typeSpec.classBuilder(className).run {
        if (isAbstract) addModifiers(KModifier.ABSTRACT)
        if (isPrivate) addModifiers(KModifier.PRIVATE)

        if (extends != null) {
            superclass(extends)
        }

        nodes.forEach { processNode(it) }

        return@run build()
    }

    private fun TypeSpec.Builder.processNode(node: TranslationNode) = when (node) {
        is TranslationNode.LeafNode -> addLeafNodeMember(node)
        is TranslationNode.MapNode -> addMapNodeMembers(node)
        is TranslationNode.ListNode -> addListNodeMembers(node)
    }

    private fun TypeSpec.Builder.addLeafNodeMember(node: TranslationNode.LeafNode) =
        if (node.arguments.isNotEmpty()) addFunction(specFactory.createFunction(node))
        else addProperty(specFactory.createProperty(node))

    private fun TypeSpec.Builder.addMapNodeMembers(node: TranslationNode.MapNode) {
        val nestedClassName = className(node.key.toPascalCase())

        addProperty(specFactory.createNestedProperty(node.key, nestedClassName))
        addType(buildClass(nestedClassName, node.children))
    }

    private fun TypeSpec.Builder.addListNodeMembers(node: TranslationNode.ListNode) {
        // TODO: Implement ListNode support
        // For now, ListNode translations are not yet fully supported in the generator
        throw UnsupportedOperationException(
            "ListNode type is not yet supported for key '${node.key}'. " + "Please restructure your translations to use MapNode instead."
        )
    }
}