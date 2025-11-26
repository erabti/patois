package com.erabti.patois.plugin.application.runners

import com.erabti.patois.models.LocalizationConfig
import com.erabti.patois.plugin.application.parsers.YamlInputParser
import com.erabti.patois.plugin.models.PatoisConfig
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.io.File

class GenerateTranslationRunnerTest : FunSpec({

    context("Locale extraction from filename") {
        test("should extract simple locale") {
            val runner = createRunner()
            val locale = runner.javaClass.getDeclaredMethod(
                "extractLocale",
                String::class.java
            ).apply { isAccessible = true }.invoke(runner, "en.yaml") as LocalizationConfig
            
            locale shouldBe LocalizationConfig("EN")
        }

        test("should extract locale from .i18n.yaml files") {
            val runner = createRunner()
            val locale = runner.javaClass.getDeclaredMethod(
                "extractLocale",
                String::class.java
            ).apply { isAccessible = true }.invoke(runner, "de.i18n.yaml") as LocalizationConfig
            
            locale shouldBe LocalizationConfig("DE")
        }

        test("should extract locale with country code (dash)") {
            val runner = createRunner()
            val locale = runner.javaClass.getDeclaredMethod(
                "extractLocale",
                String::class.java
            ).apply { isAccessible = true }.invoke(runner, "zh-CN.yaml") as LocalizationConfig
            
            locale shouldBe LocalizationConfig("ZH", "CN")
        }

        test("should extract locale with country code (underscore)") {
            val runner = createRunner()
            val locale = runner.javaClass.getDeclaredMethod(
                "extractLocale",
                String::class.java
            ).apply { isAccessible = true }.invoke(runner, "pt_BR.i18n.json") as LocalizationConfig
            
            locale shouldBe LocalizationConfig("PT", "BR")
        }

        test("should extract locale from .json files") {
            val runner = createRunner()
            val locale = runner.javaClass.getDeclaredMethod(
                "extractLocale",
                String::class.java
            ).apply { isAccessible = true }.invoke(runner, "fr.json") as LocalizationConfig
            
            locale shouldBe LocalizationConfig("FR")
        }

        test("should extract locale from .yml files") {
            val runner = createRunner()
            val locale = runner.javaClass.getDeclaredMethod(
                "extractLocale",
                String::class.java
            ).apply { isAccessible = true }.invoke(runner, "es.yml") as LocalizationConfig
            
            locale shouldBe LocalizationConfig("ES")
        }
    }

    context("File discovery") {
        test("should discover YAML files") {
            val tempDir = createTempDir()
            try {
                File(tempDir, "en.yaml").writeText("greeting: Hello")
                File(tempDir, "de.yaml").writeText("greeting: Hallo")
                File(tempDir, "fr.yml").writeText("greeting: Bonjour")
                File(tempDir, "readme.txt").writeText("This should be ignored")
                File(tempDir, "es.json").writeText("{\"greeting\": \"Hola\"}")

                val config = PatoisConfig(
                    className = "TestStrings",
                    packageName = "com.test",
                    inputDir = tempDir,
                    outputDir = createTempDir(),
                    baseLocale = "en"
                )
                val runner = GenerateTranslationRunner(config)
                @Suppress("UNCHECKED_CAST")
                val files = runner.javaClass.getDeclaredMethod("discoverTranslationFiles")
                    .apply { isAccessible = true }
                    .invoke(runner) as Array<File>

                files.size shouldBe 3
            } finally {
                tempDir.deleteRecursively()
            }
        }

        test("should throw when no translation files found") {
            val tempDir = createTempDir()
            try {
                val config = PatoisConfig(
                    className = "TestStrings",
                    packageName = "com.test",
                    inputDir = tempDir,
                    outputDir = createTempDir(),
                    baseLocale = "en"
                )
                val runner = GenerateTranslationRunner(config)

                val exception = shouldThrow<IllegalStateException> {
                    runner.run()
                }
                exception.message shouldContain "No translation files found"
            } finally {
                tempDir.deleteRecursively()
            }
        }

        test("should throw when base locale not found") {
            val tempDir = createTempDir()
            try {
                File(tempDir, "de.yaml").writeText("greeting: Hallo")
                File(tempDir, "fr.yaml").writeText("greeting: Bonjour")

                val config = PatoisConfig(
                    className = "TestStrings",
                    packageName = "com.test",
                    inputDir = tempDir,
                    outputDir = createTempDir(),
                    baseLocale = "en"
                )
                val runner = GenerateTranslationRunner(config)

                val exception = shouldThrow<IllegalStateException> {
                    runner.run()
                }
                exception.message shouldContain "Base locale"
                exception.message shouldContain "not found"
            } finally {
                tempDir.deleteRecursively()
            }
        }
    }
})

private fun createRunner(): GenerateTranslationRunner {
    val config = PatoisConfig(
        className = "TestStrings",
        packageName = "com.test",
        inputDir = File("/tmp"),
        outputDir = File("/tmp"),
        baseLocale = "en"
    )
    return GenerateTranslationRunner(config, YamlInputParser())
}

private fun createTempDir(): File {
    return File.createTempFile("patois-test", "").apply {
        delete()
        mkdir()
    }
}
