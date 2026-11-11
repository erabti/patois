package io.github.erabti.patois.plugin.runners

import io.github.erabti.patois.models.PatoisConfig
import io.github.erabti.patois.plugin.application.runners.GenerateTranslationRunner
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class RepeatedArgumentTest : FunSpec({
    test("a text that uses an argument twice takes it as one parameter") {
        val input = tempdir()
        val output = tempdir()
        input.resolve("en.yaml").writeText("""again: "{name} and again {name}"""")

        GenerateTranslationRunner(
            config = PatoisConfig(
                className = "AppStrings",
                packageName = "com.example",
                inputDir = input.absolutePath,
                outputDir = output.absolutePath,
                baseLocale = "en",
                enumName = "AppLocale",
                resolverName = "AppLocaleResolver",
            ),
            logger = {},
        ).run()

        val generated = output.resolve("com/example/AppStringsEn.kt").readText()
        generated shouldContain "fun again(name: String): String"
        Regex("""\${'$'}\{?name""").findAll(generated).count() shouldBe 2
    }
})
