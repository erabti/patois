package com.erabti.patois.plugin.application.runners

import com.erabti.patois.models.AppLocale
import com.erabti.patois.plugin.application.generators.AbstractSpecStrategy
import com.erabti.patois.plugin.application.generators.ConcreteSpecStrategy
import com.erabti.patois.plugin.application.generators.KotlinGenerator
import com.erabti.patois.plugin.application.generators.TranslationClassSpecBuilder
import com.erabti.patois.plugin.application.parsers.InputParser
import com.erabti.patois.plugin.application.parsers.YamlInputParser
import com.erabti.patois.plugin.models.PatoisConfig
import com.erabti.patois.plugin.models.TranslationNode
import com.squareup.kotlinpoet.ClassName
import java.io.File

class GenerateTranslationRunner(
    val config: PatoisConfig,
    val yamlReader: InputParser = YamlInputParser(),
) {
    fun run() {
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

        val baseLocale = config.baseLocale?.let(AppLocale::fromLanguageTag) ?: localeTranslations.first().locale
        val baseTranslation = localeTranslations.firstOrNull { it.locale == baseLocale } ?: throw IllegalStateException(
            "Base locale '$baseLocale' not found in the translation files."
        )

        // Generate abstract base class
        val abstractStrategy = AbstractSpecStrategy(config)
        val abstractBuilder = TranslationClassSpecBuilder(abstractStrategy)
        val abstractGenerator = KotlinGenerator(config, abstractBuilder)

        val baseFileName = config.className
        val baseFileSpec = abstractGenerator.generateTranslationFile(baseFileName, baseTranslation.nodes)
        baseFileSpec.writeTo(config.outputDir)

        // Generate concrete implementations for each locale
        val abstractClassName = ClassName(config.packageName, config.className)
        localeTranslations.forEach { (locale, _, nodes) ->
            val concreteStrategy = ConcreteSpecStrategy(locale, abstractClassName, config)
            val concreteBuilder = TranslationClassSpecBuilder(concreteStrategy)
            val concreteGenerator = KotlinGenerator(config, concreteBuilder)

            val outputFileName = getLocaleFileName(locale)
            val fileSpec = concreteGenerator.generateTranslationFile(outputFileName, nodes, abstractClassName)
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
        val unwantedSuffixes = listOf(".yaml", ".yml", ".json", ".i18n")
        val withoutExtensions = unwantedSuffixes.fold(fileName) { name, suffix -> name.removeSuffix(suffix) }
        return AppLocale.fromLanguageTag(withoutExtensions)
    }

    private data class LocaleTranslation(
        val locale: AppLocale,
        val fileName: String,
        val nodes: List<TranslationNode>,
    )
}