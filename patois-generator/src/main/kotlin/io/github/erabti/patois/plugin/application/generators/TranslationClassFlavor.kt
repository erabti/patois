package io.github.erabti.patois.plugin.application.generators

import io.github.erabti.patois.plugin.application.runners.RunnerContext
import io.github.erabti.patois.plugin.models.TemplateArgument
import io.github.erabti.patois.plugin.models.TranslationNode.LeafNode
import io.github.erabti.patois.plugin.models.TranslationNode.ListNode
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
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

    fun convertTemplateArg(value: String, args: List<TemplateArgument>): String {
        val namesByPlaceholder = args.associate { config.argumentPattern.wrapWithPattern(it.name) to it.name }
        if (namesByPlaceholder.isEmpty()) return value.escapeDollars()

        val placeholder = namesByPlaceholder.keys
            .sortedByDescending { it.length }
            .joinToString("|") { Regex.escape(it) }
            .toRegex()

        val template = StringBuilder()
        var textStart = 0
        for (match in placeholder.findAll(value)) {
            template.append(value.substring(textStart, match.range.first).escapeDollars())
            template.append("\${").append(CodeBlock.of("%N", namesByPlaceholder.getValue(match.value))).append("}")
            textStart = match.range.last + 1
        }
        template.append(value.substring(textStart).escapeDollars())

        return template.toString()
    }
}

private fun String.escapeDollars() = replace("$", "\${'$'}")


internal val TranslationClassFlavor.config get() = context.config

