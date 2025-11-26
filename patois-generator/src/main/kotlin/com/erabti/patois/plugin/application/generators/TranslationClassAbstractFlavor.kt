package com.erabti.patois.plugin.application.generators

import com.erabti.patois.plugin.application.runners.RunnerContext
import com.erabti.patois.plugin.models.TranslationNode
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

internal class TranslationClassAbstractFlavor(override val context: RunnerContext) : TranslationClassFlavor {
    override fun TypeSpec.Builder.configureClass(isRoot: Boolean) {
        addModifiers(KModifier.SEALED)
    }


    override fun leafProperty(node: TranslationNode.LeafNode): PropertySpec {
        return PropertySpec.Companion.builder(node.key, String::class).addModifiers(KModifier.ABSTRACT).build()
    }

    override fun leafFunction(node: TranslationNode.LeafNode): FunSpec {
        return FunSpec.Companion.builder(node.key).addModifiers(KModifier.ABSTRACT).apply {
            returns(String::class)
            node.arguments.forEach { addParameter(it.name, String::class) }
        }.build()
    }

    override fun nestedProperty(key: String, abstractType: ClassName, implType: ClassName): PropertySpec {
        return PropertySpec.Companion.builder(key, abstractType).addModifiers(KModifier.ABSTRACT).build()
    }

    override fun listProperty(node: TranslationNode.ListNode): PropertySpec {
        val listType = List::class.asClassName().parameterizedBy(String::class.asClassName())
        return PropertySpec.Companion.builder(node.key, listType).addModifiers(KModifier.ABSTRACT).build()
    }

    override fun nestedClassNames(
        nestedBaseName: String,
        parentClassName: ClassName,
    ): Pair<ClassName, ClassName?> = ClassName("", nestedBaseName) to null
}