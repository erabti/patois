package com.erabti.patois.plugin.application.generators

import com.erabti.patois.plugin.models.TranslationNode
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec

internal object TranslationSpecFactory {
    fun createProperty(node: TranslationNode.LeafNode) = PropertySpec.builder(node.key, String::class).run {
        addModifiers(KModifier.ABSTRACT)

        build()
    }

    fun createFunction(node: TranslationNode.LeafNode) = FunSpec.builder(node.key).run {
        addModifiers(KModifier.ABSTRACT)
        returns(String::class)
        node.arguments.forEach { arg ->
            addParameter(arg.name, String::class)
        }

        build()
    }

    fun createNestedProperty(key: String, type: ClassName) = PropertySpec.builder(key, type).run {
        addModifiers(KModifier.ABSTRACT)

        build()
    }
}