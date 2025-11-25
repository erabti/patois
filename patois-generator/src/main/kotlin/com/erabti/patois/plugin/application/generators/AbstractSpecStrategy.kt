package com.erabti.patois.plugin.application.generators

import com.erabti.patois.plugin.models.PatoisConfig
import com.erabti.patois.plugin.models.TranslationNode
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

internal class AbstractSpecStrategy(override val config: PatoisConfig) : SpecBuildingStrategy() {
    override fun buildProperty(node: TranslationNode.LeafNode) = PropertySpec.builder(node.key, String::class).run {
        addModifiers(KModifier.ABSTRACT)
        build()
    }

    override fun buildFunction(node: TranslationNode.LeafNode) = FunSpec.builder(node.key).run {
        addModifiers(KModifier.ABSTRACT)
        returns(String::class)
        node.arguments.forEach { arg ->
            addParameter(arg.name, String::class)
        }
        build()
    }

    override fun buildNestedProperty(key: String, abstractType: ClassName, implType: ClassName) =
        PropertySpec.builder(key, abstractType).run {
            addModifiers(KModifier.ABSTRACT)

            build()
        }

    override fun buildListProperty(node: TranslationNode.ListNode): PropertySpec {
        val listType = List::class.asClassName().parameterizedBy(String::class.asClassName())
        return PropertySpec.builder(node.key, listType)
            .addModifiers(KModifier.ABSTRACT)
            .build()
    }

    override fun configureClass(builder: TypeSpec.Builder, isRoot: Boolean) {
        builder.addModifiers(KModifier.ABSTRACT)
    }

    override fun getNestedClassNames(nestedBaseName: String, parentClassName: ClassName): Pair<ClassName, ClassName?> {
        val nestedClassName = className(nestedBaseName)
        return nestedClassName to null
    }
}
