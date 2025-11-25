package com.erabti.patois.plugin.tasks

import com.erabti.patois.plugin.generators.KotlinGenerator
import com.erabti.patois.plugin.models.PatoisPluginExtension
import com.erabti.patois.plugin.models.TranslationNode
import com.erabti.patois.plugin.models.toConfig
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction

abstract class GenerateTranslations : DefaultTask() {

    @TaskAction
    fun execute() {
        val extension = project.extensions.findByType(PatoisPluginExtension::class.java)!!
        val config = extension.toConfig()

        val outputDir = config.outputDir

        val nodes = getTranslationNodes()
        val gen = KotlinGenerator(config = config, nodes = nodes)
        val fileSpec = gen.generate()

        fileSpec.writeTo(outputDir)

        project.extensions.getByType(SourceSetContainer::class.java).getByName("main").java.srcDir(outputDir)
    }


}

private fun getTranslationNodes(): List<TranslationNode> {
    return listOf(
        TranslationNode(
            key = "hello", value = "Hello", children = emptyList()
        )
    )
}