package com.erabti.patois.plugin.application.generators

import com.erabti.patois.plugin.models.PatoisConfig
import com.erabti.patois.plugin.models.TranslationNode
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec

internal class KotlinGenerator(
    config: PatoisConfig,
    private val classSpecBuilder: TranslationClassSpecBuilder,
) {
    private val packageName = config.packageName

    fun generateTranslationFile(fileName: String, nodes: List<TranslationNode>, extendsClass: ClassName? = null): FileSpec {
        return FileSpec.builder(packageName, fileName).apply {
            addSupress("RedundantVisibilityModifier")
            val className = className(fileName)
            val rootClass = classSpecBuilder.build(className = className, nodes = nodes, extendsClass = extendsClass)

            addType(rootClass)
        }.build()
    }
}

