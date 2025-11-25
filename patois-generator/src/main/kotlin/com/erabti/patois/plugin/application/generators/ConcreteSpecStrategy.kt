package com.erabti.patois.plugin.application.generators

import com.erabti.patois.models.AppLocale
import com.erabti.patois.plugin.models.PatoisConfig
import com.erabti.patois.plugin.models.TranslationNode
import com.erabti.patois.plugin.utils.Constants
import com.squareup.kotlinpoet.*

internal class ConcreteSpecStrategy(
    private val locale: AppLocale,
    private val baseClassName: ClassName,
    override val config: PatoisConfig,
) : SpecBuildingStrategy() {
    override val shouldAddLocaleProperty = true

    override fun buildProperty(node: TranslationNode.LeafNode) = PropertySpec.builder(node.key, String::class).run {
        addModifiers(KModifier.OVERRIDE)
        initializer("%S", node.value)
        build()
    }

    override fun buildFunction(node: TranslationNode.LeafNode) = FunSpec.builder(node.key).run {
        addModifiers(KModifier.OVERRIDE)
        returns(String::class)

        node.arguments.forEach { arg ->
            addParameter(arg.name, String::class)
        }

        val convertedTemplate = convertTemplateArg(node.value, node.arguments)
        addCode(CodeBlock.of("return %P", convertedTemplate))

        build()
    }

    override fun buildNestedProperty(key: String, abstractType: ClassName, implType: ClassName): PropertySpec {
        val fullyQualifiedAbstractType = getFullyQualifiedNestedClassName(abstractType.simpleName)

        return PropertySpec.builder(key, fullyQualifiedAbstractType).run {
            addModifiers(KModifier.OVERRIDE)
            initializer("%T()", implType)
            build()
        }
    }

    fun getFullyQualifiedNestedClassName(simpleName: String) =
        ClassName(baseClassName.packageName, simpleName)

    override fun configureClass(builder: TypeSpec.Builder, isRoot: Boolean) {
        if (!isRoot) {
            builder.addModifiers(KModifier.PRIVATE)
        }
    }

    override fun getNestedClassNames(nestedBaseName: String, parentClassName: ClassName): Pair<ClassName, ClassName?> {
        val suffix = locale.pascalCaseTag
        val implClassName = ClassName("", nestedBaseName + suffix)
        val extendsClassName = getFullyQualifiedNestedClassName(nestedBaseName)
        return implClassName to extendsClassName
    }

    override fun buildLocaleProperty(): PropertySpec {
        val localeClassName = ClassName(Constants.PATOIS_CORE_PACKAGE, "AppLocale")

        return PropertySpec.builder("locale", localeClassName).run {
            addModifiers(KModifier.OVERRIDE)

            if (locale.countryCode != null) {
                initializer("%T(%S, %S)", localeClassName, locale.languageCode, locale.countryCode)
            } else {
                initializer("%T(%S)", localeClassName, locale.languageCode)
            }

            build()
        }
    }
}
