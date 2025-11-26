package com.erabti.patois.plugin.application.generators

import com.erabti.patois.models.LocalizationConfig
import com.erabti.patois.plugin.application.runners.RunnerContext
import com.erabti.patois.plugin.models.TranslationNode
import com.erabti.patois.plugin.utils.pascalCaseTag
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

internal class TranslationClassConcreteFlavor(
    private val locale: LocalizationConfig,
    private val baseClassName: ClassName,
    override val context: RunnerContext,
) : TranslationClassFlavor {
    private val listType = List::class.asClassName().parameterizedBy(String::class.asClassName())

    override fun TypeSpec.Builder.configureClass(isRoot: Boolean) {
        if (!isRoot) {
            addModifiers(KModifier.PRIVATE)
        }
    }

    override fun leafProperty(node: TranslationNode.LeafNode): PropertySpec {
        return PropertySpec.Companion.builder(node.key, String::class).addModifiers(KModifier.OVERRIDE)
            .initializer("%S", node.value).build()
    }

    override fun leafFunction(node: TranslationNode.LeafNode): FunSpec {
        val convertedTemplate = convertTemplateArg(node.value, node.arguments)
        return FunSpec.Companion.builder(node.key).addModifiers(KModifier.OVERRIDE).apply {
            returns(String::class)
            node.arguments.forEach { addParameter(it.name, String::class) }
            addCode(CodeBlock.Companion.of("return %P", convertedTemplate))
        }.build()
    }

    override fun nestedProperty(key: String, abstractType: ClassName, implType: ClassName): PropertySpec {
        val fullyQualifiedAbstractType = ClassName(baseClassName.packageName, abstractType.simpleName)
        return PropertySpec.Companion.builder(key, fullyQualifiedAbstractType).addModifiers(KModifier.OVERRIDE)
            .initializer("%T()", implType).build()
    }

    override fun listProperty(node: TranslationNode.ListNode): PropertySpec {
        val leafItems = node.items.filterIsInstance<TranslationNode.LeafNode>()
        val initializerCode = leafItems.joinToString(", ") { "%S" }
        val values = leafItems.map { it.value }.toTypedArray()

        return PropertySpec.Companion.builder(node.key, listType).addModifiers(KModifier.OVERRIDE)
            .initializer("listOf($initializerCode)", *values).build()
    }

    override fun nestedClassNames(
        nestedBaseName: String,
        parentClassName: ClassName,
    ): Pair<ClassName, ClassName?> {
        val suffix = locale.pascalCaseTag
        val implClassName = ClassName("", nestedBaseName + suffix)
        val extendsClassName = ClassName(baseClassName.packageName, nestedBaseName)
        return implClassName to extendsClassName
    }

    override fun localeProperty(): PropertySpec {
        val localeClassName = LocalizationConfig::class.asClassName()

        return PropertySpec.Companion.builder("locale", localeClassName).apply {
            addModifiers(KModifier.OVERRIDE)

            if (locale.countryCode != null && locale.scriptCode != null) {
                initializer(
                    "%T(languageCode = %S, countryCode = %S, scriptCode = %S)",
                    localeClassName,
                    locale.languageCode,
                    locale.countryCode,
                    locale.scriptCode
                )
                return@apply
            }
            if (locale.scriptCode != null) {
                initializer(
                    "%T(languageCode = %S, scriptCode = %S)", localeClassName, locale.languageCode, locale.scriptCode
                )
                return@apply
            }
            if (locale.countryCode != null) {
                initializer(
                    "%T(languageCode = %S, countryCode = %S)", localeClassName, locale.languageCode, locale.countryCode
                )

                return@apply
            }

            initializer("%T(%S)", localeClassName, locale.languageCode)
        }.build()
    }
}