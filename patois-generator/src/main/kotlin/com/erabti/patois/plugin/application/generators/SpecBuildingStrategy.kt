package com.erabti.patois.plugin.application.generators

import com.erabti.patois.plugin.models.PatoisConfig
import com.erabti.patois.plugin.models.TemplateArgument
import com.erabti.patois.plugin.models.TranslationNode
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

internal sealed class SpecBuildingStrategy() {
    protected abstract val config: PatoisConfig
    open val shouldAddLocaleProperty = false

    abstract fun configureClass(builder: TypeSpec.Builder, isRoot: Boolean)

    abstract fun buildProperty(node: TranslationNode.LeafNode): PropertySpec

    abstract fun buildFunction(node: TranslationNode.LeafNode): FunSpec

    abstract fun buildNestedProperty(key: String, abstractType: ClassName, implType: ClassName): PropertySpec

    open fun buildLocaleProperty(): PropertySpec? = null

    abstract fun getNestedClassNames(nestedBaseName: String, parentClassName: ClassName): Pair<ClassName, ClassName?>

    protected fun convertTemplateArg(value: String, args: List<TemplateArgument>): String {
        var result = value

        args.forEach { arg ->
            val pattern = config.argumentPattern.wrapWithPattern(arg.name)
            val name = arg.name
            result = result.replace(pattern, "$$name")
        }

        return result
    }
}
