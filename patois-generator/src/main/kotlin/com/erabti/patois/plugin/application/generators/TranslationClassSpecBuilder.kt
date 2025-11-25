package com.erabti.patois.plugin.application.generators

import com.erabti.patois.plugin.models.TranslationNode
import com.erabti.patois.plugin.utils.Constants
import com.erabti.patois.util.toPascalCase
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec

internal class TranslationClassSpecBuilder(
    private val strategy: SpecBuildingStrategy,
    private val typeSpec: TypeSpec.Companion = TypeSpec,
) {
    fun build(className: ClassName, nodes: List<TranslationNode>, extendsClass: ClassName? = null): TypeSpec {
        val superClass = extendsClass ?: Constants.BASE_APP_STRINGS_CLASS_NAME
        return buildClass(className, nodes, superClass, isRoot = true)
    }

    private fun buildClass(
        className: ClassName,
        nodes: List<TranslationNode>,
        extends: ClassName? = null,
        isRoot: Boolean = false,
    ): TypeSpec = typeSpec.classBuilder(className).run {
        strategy.configureClass(this, isRoot)

        if (extends != null) {
            superclass(extends)
        }

        if (isRoot && strategy.shouldAddLocaleProperty) {
            strategy.buildLocaleProperty()?.let { addProperty(it) }
        }

        nodes.forEach { processNode(it, className) }

        return@run build()
    }

    private fun TypeSpec.Builder.processNode(node: TranslationNode, parentClassName: ClassName) = when (node) {
        is TranslationNode.LeafNode -> addLeafNodeMember(node)
        is TranslationNode.MapNode -> addMapNodeMembers(node, parentClassName)
        is TranslationNode.ListNode -> addListNodeMembers(node)
    }

    private fun TypeSpec.Builder.addLeafNodeMember(node: TranslationNode.LeafNode) {
        if (node.arguments.isNotEmpty()) {
            addFunction(strategy.buildFunction(node))
        } else {
            addProperty(strategy.buildProperty(node))
        }
    }

    private fun TypeSpec.Builder.addMapNodeMembers(node: TranslationNode.MapNode, parentClassName: ClassName) {
        val nestedBaseName = node.key.toPascalCase()
        val abstractClassName = className(nestedBaseName)

        val (implClassName, extendsClass) = strategy.getNestedClassNames(nestedBaseName, parentClassName)

        addProperty(strategy.buildNestedProperty(node.key, abstractClassName, implClassName))
        addType(buildClass(implClassName, node.children, extendsClass))
    }

    private fun TypeSpec.Builder.addListNodeMembers(node: TranslationNode.ListNode) {
        throw UnsupportedOperationException(
            "ListNode type is not yet supported for key '${node.key}'. " +
                    "Please restructure your translations to use MapNode instead."
        )
    }
}