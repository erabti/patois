package com.erabti.patois.plugin.application.generators

import com.erabti.patois.plugin.application.runners.RunnerContext
import com.erabti.patois.plugin.models.TemplateArgument
import com.erabti.patois.plugin.models.TranslationNode.LeafNode
import com.erabti.patois.plugin.models.TranslationNode.ListNode
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec


internal sealed interface TranslationClassFlavor {
    val context: RunnerContext

    fun TypeSpec.Builder.configureClass(isRoot: Boolean)

    fun leafProperty(node: LeafNode): PropertySpec

    fun leafFunction(node: LeafNode): FunSpec

    fun nestedProperty(key: String, abstractType: ClassName, implType: ClassName): PropertySpec

    fun listProperty(node: ListNode): PropertySpec

    fun nestedClassNames(nestedBaseName: String, parentClassName: ClassName): Pair<ClassName, ClassName?>

    fun localeProperty(): PropertySpec? = null

    fun convertTemplateArg(value: String, args: List<TemplateArgument>) = args.fold(value) { result, arg ->
        val pattern = config.argumentPattern.wrapWithPattern(arg.name)
        val replacement = "$${arg.name}"
        result.replace(pattern, replacement)
    }
}


internal val TranslationClassFlavor.config get() = context.config

