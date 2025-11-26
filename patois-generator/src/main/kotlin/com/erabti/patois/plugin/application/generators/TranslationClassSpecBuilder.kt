package com.erabti.patois.plugin.application.generators

import com.erabti.patois.models.BaseAppStrings
import com.erabti.patois.plugin.models.TranslationNode
import com.erabti.patois.plugin.utils.Constants
import com.erabti.patois.util.toPascalCase
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName

internal class TranslationClassSpecBuilder(
    private val flavor: TranslationClassFlavor,
    private val typeSpec: TypeSpec.Companion = TypeSpec,
) {
    fun build(
        className: ClassName,
        nodes: List<TranslationNode>,
        extendsClass: ClassName? = null,
        builder: TypeSpec.Builder.() -> Unit = { },
    ): TypeSpec {
        val superClass = extendsClass ?: BaseAppStrings::class.asClassName()
        return buildClass(className, nodes, superClass, isRoot = true, builder)
    }

    private fun buildClass(
        className: ClassName,
        nodes: List<TranslationNode>,
        extends: ClassName? = null,
        isRoot: Boolean = false,
        builder: TypeSpec.Builder.() -> Unit = { },
    ): TypeSpec = typeSpec.classBuilder(className).run {
        with(flavor) { configureClass(isRoot) }

        if (extends != null) {
            superclass(extends)
        }

        if (isRoot) {
            flavor.localeProperty()?.let { addProperty(it) }
        }

        nodes.forEach { processNode(it, className) }

        builder(this)

        return@run build()
    }

    private fun TypeSpec.Builder.processNode(node: TranslationNode, parentClassName: ClassName) = when (node) {
        is TranslationNode.LeafNode -> addLeafNodeMember(node)
        is TranslationNode.MapNode -> addMapNodeMembers(node, parentClassName)
        is TranslationNode.ListNode -> addListNodeMembers(node)
    }

    private fun TypeSpec.Builder.addLeafNodeMember(node: TranslationNode.LeafNode) {
        if (node.arguments.isNotEmpty()) {
            addFunction(flavor.leafFunction(node))
        } else {
            addProperty(flavor.leafProperty(node))
        }
    }

    private fun TypeSpec.Builder.addMapNodeMembers(node: TranslationNode.MapNode, parentClassName: ClassName) {
        val nestedBaseName = node.key.toPascalCase()
        val abstractClassName = ClassName("", nestedBaseName)

        val (implClassName, extendsClass) = flavor.nestedClassNames(nestedBaseName, parentClassName)

        addProperty(flavor.nestedProperty(node.key, abstractClassName, implClassName))
        addType(buildClass(implClassName, node.children, extendsClass))
    }

    private fun TypeSpec.Builder.addListNodeMembers(node: TranslationNode.ListNode) {
        val hasComplexItems = node.items.any { it !is TranslationNode.LeafNode }

        if (hasComplexItems) {
            throw UnsupportedOperationException(
                "ListNode with complex nested items (maps or nested lists) is not yet supported for key '${node.key}'. " +
                        "Only simple string lists are currently supported."
            )
        }

        addProperty(flavor.listProperty(node))
    }
}