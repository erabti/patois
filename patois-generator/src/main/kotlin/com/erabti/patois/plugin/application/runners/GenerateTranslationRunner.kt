package com.erabti.patois.plugin.application.runners

import com.erabti.patois.models.AppLocale
import com.erabti.patois.plugin.application.generators.KotlinGenerator
import com.erabti.patois.plugin.application.parsers.InputParser
import com.erabti.patois.plugin.application.parsers.YamlInputParser
import com.erabti.patois.plugin.models.PatoisConfig
import com.erabti.patois.plugin.models.TranslationNode
import java.io.File

class GenerateTranslationRunner(
    val config: PatoisConfig,
    val yamlReader: InputParser = YamlInputParser(),
) {
    fun run() {
        val kotlinGenerator = KotlinGenerator(config)

        val yamlFiles = discoverTranslationFiles()

        if (yamlFiles.isEmpty()) {
            throw IllegalStateException("No translation files found in ${config.inputDir}")
        }

        val localeTranslations = yamlFiles.map { file ->
            val locale = extractLocale(file.name)
            val content = file.readText()
            val nodes = yamlReader.read(content)
            LocaleTranslation(locale, file.name, nodes)
        }

        val baseLocale = config.baseLocale ?: localeTranslations.first().locale

        if (!localeTranslations.any { it.locale == baseLocale }) {
            throw IllegalStateException("Base locale '$baseLocale' not found. Available: ${localeTranslations.map { it.locale }}")
        }

        localeTranslations.forEach { (locale, inputFileName, nodes) ->
            val outputFileName = getLocaleFileName(locale)
            val fileSpec = kotlinGenerator.generate(outputFileName, nodes)
            fileSpec.writeTo(config.outputDir)
        }

        println("Generated translations for ${localeTranslations.size} locales: ${localeTranslations.map { it.locale }}")
    }


    private fun getLocaleFileName(locale: AppLocale): String {
        val className = config.className
        return "$className${locale.pascalCaseTag}"
    }

    private fun discoverTranslationFiles(): Array<File> {
        return config.inputDir.listFiles { file ->
            file.isFile && file.extension in listOf("yaml", "yml")
        } ?: emptyArray()
    }


    private fun extractLocale(fileName: String): AppLocale {
        val withoutExtensions =
            fileName.removeSuffix(".yaml").removeSuffix(".yml").removeSuffix(".json").removeSuffix(".i18n")

        return AppLocale.fromLanguageTag(withoutExtensions)
    }

    private data class LocaleTranslation(
        val locale: AppLocale,
        val fileName: String,
        val nodes: List<TranslationNode>,
    )
}