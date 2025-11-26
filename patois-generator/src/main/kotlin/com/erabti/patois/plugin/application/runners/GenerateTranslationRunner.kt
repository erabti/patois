package com.erabti.patois.plugin.application.runners

import com.erabti.patois.models.LocalizationConfig
import com.erabti.patois.models.PatoisConfig
import com.erabti.patois.plugin.application.generators.KotlinGenerator
import com.erabti.patois.plugin.application.parsers.InputParser
import com.erabti.patois.plugin.application.parsers.YamlInputParser
import com.erabti.patois.plugin.models.inputDirFile
import com.erabti.patois.plugin.utils.pascalCaseTag
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

        val translations = yamlFiles.map { file ->
            val locale = extractLocale(file.name)
            val content = file.readText()
            val nodes = yamlReader.read(content)
            LocaleTranslation(locale, file.name, nodes)
        }

        val baseTranslation = getBaseTranslation(translations)

        val context = RunnerContext(
            config = config,
            translations = translations,
            baseTranslation = baseTranslation,
        )

        val kotlinGen = KotlinGenerator(context)

        kotlinGen.generateBaseFile()
        kotlinGen.generateConcreteFiles()

        println("Generated translations for ${translations.size} locales: ${translations.map { it.locale.pascalCaseTag }}")
    }

    private fun getBaseTranslation(translations: List<LocaleTranslation>): LocaleTranslation {
        val baseLocale = config.baseLocale?.let(LocalizationConfig::fromLanguageTag) ?: translations.first().locale

        return translations.firstOrNull { it.locale == baseLocale } ?: throw IllegalStateException(
            "Base locale '$baseLocale' not found in the translation files."
        )
    }


    private fun discoverTranslationFiles(): Array<File> {
        return config.inputDirFile.listFiles { file ->
            file.isFile && file.extension in listOf("yaml", "yml")
        } ?: emptyArray()
    }


    private fun extractLocale(fileName: String): LocalizationConfig {
        val unwantedSuffixes = listOf(".yaml", ".yml", ".json", ".i18n")
        val withoutExtensions = unwantedSuffixes.fold(fileName) { name, suffix -> name.removeSuffix(suffix) }
        return LocalizationConfig.fromLanguageTag(withoutExtensions)
    }
}