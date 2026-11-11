package io.github.erabti.patois.plugin.runners

import io.github.erabti.patois.models.PatoisConfig
import io.github.erabti.patois.plugin.application.runners.GenerateTranslationRunner
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.string.shouldContain

class TemplateEscapingTest : FunSpec({
    fun generate(yaml: String): String {
        val input = tempdir()
        val output = tempdir()
        input.resolve("en.yaml").writeText(yaml)

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

        return output.resolve("com/example/AppStringsEn.kt").readText()
    }

    val dollar = "\${'$'}"

    test("an argument followed by a letter stays a separate name") {
        generate("""weight: "{count}kg"""") shouldContain "\"\"\"\${count}kg\"\"\""
    }

    test("an argument followed by an Arabic letter stays a separate name") {
        generate("""weight: "{count}كغ"""") shouldContain "\"\"\"\${count}كغ\"\"\""
    }

    test("a dollar sign in a text with arguments stays text") {
        generate("""price: "It costs $5, {name}"""") shouldContain "\"\"\"It costs ${dollar}5, \${name}\"\"\""
    }

    test("a Kotlin expression in a text with arguments stays text") {
        val generated = generate("hello: 'Hi {name}, \${System.exit(1)} and \$name'")

        generated shouldContain "\"\"\"Hi \${name}, $dollar{System.exit(1)} and ${dollar}name\"\"\""
    }

    test("an argument named like a Kotlin keyword is escaped") {
        generate("""range: "{in} to {out}"""") shouldContain "\"\"\"\${`in`} to \${`out`}\"\"\""
    }

    test("a multi-line text with arguments keeps its dollar signs as text") {
        val generated = generate("multi: |\n  Pay $5\n  to {name}\n")

        generated shouldContain "|Pay ${dollar}5"
        generated shouldContain "|to \${name}"
    }
})
