package com.erabti.patois.plugin.application.generators

import com.erabti.patois.plugin.models.PatoisConfig
import com.erabti.patois.plugin.models.TranslationNode
import com.squareup.kotlinpoet.FileSpec

internal class KotlinGenerator(
    config: PatoisConfig,
    val classSpecBuilder: TranslationClassSpecBuilder = TranslationClassSpecBuilder(),
) {
    private val packageName = config.packageName

    fun generateTranslationFile(fileName: String, nodes: List<TranslationNode>): FileSpec {
        return FileSpec.builder(packageName, fileName).apply {
            addSupress("RedundantVisibilityModifier")
            val className = className(fileName)
            val rootClass = classSpecBuilder.build(className = className, nodes = nodes)

            addType(rootClass)
        }.build()
    }
}

