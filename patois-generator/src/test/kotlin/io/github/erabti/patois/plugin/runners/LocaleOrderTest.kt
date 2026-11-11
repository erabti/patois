package io.github.erabti.patois.plugin.runners

import io.github.erabti.patois.models.PatoisConfig
import io.github.erabti.patois.plugin.application.runners.GenerateTranslationRunner
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class LocaleOrderTest : FunSpec({
    fun generate(baseLocale: String?, vararg locales: String): String {
        val input = tempdir()
        val output = tempdir()
        locales.forEach { input.resolve("$it.yaml").writeText("greeting: Hello") }

        GenerateTranslationRunner(
            config = PatoisConfig(
                className = "AppStrings",
                packageName = "com.example",
                inputDir = input.absolutePath,
                outputDir = output.absolutePath,
                baseLocale = baseLocale,
                enumName = "AppLocale",
                resolverName = "AppLocaleResolver",
            ),
            logger = {},
        ).run()

        return output.resolve("com/example/AppStrings.kt").readText()
    }

    test("the generated locales follow the file names, not the order the files were written in") {
        val generated = generate("en", "tr", "en", "de", "fr", "ar", "it", "es")

        val enumBody = generated.substringAfter("enum class AppLocale").substringBefore(";")
        val locales = Regex("""^\s+([A-Z_]+),$""", RegexOption.MULTILINE).findAll(enumBody).map { it.groupValues[1] }

        locales.toList() shouldBe listOf("AR", "DE", "EN", "ES", "FR", "IT", "TR")
    }

    test("without a base locale, the first file name is the base") {
        val generated = generate(null, "fr", "en", "ar")

        generated shouldContain "override val defaultLocale: AppLocale = AppLocale.AR"
    }
})
