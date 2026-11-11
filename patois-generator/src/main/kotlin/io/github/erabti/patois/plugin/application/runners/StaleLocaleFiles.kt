package io.github.erabti.patois.plugin.application.runners

import io.github.erabti.patois.models.PatoisConfig
import io.github.erabti.patois.plugin.models.outputDirFile
import io.github.erabti.patois.plugin.utils.pascalCaseTag
import java.io.File

internal fun removeStaleLocaleFiles(config: PatoisConfig, translations: List<LocaleTranslation>) {
    val generated = translations.map { "${config.className}${it.locale.pascalCaseTag}.kt" }.toSet()
    val localeClass = Regex("""class\s+\w+\s*:\s*${Regex.escape(config.className)}\(\)""")
    val packageDir = config.outputDirFile.resolve(config.packageName.replace('.', File.separatorChar))

    packageDir.listFiles { file -> file.isFile && file.extension == "kt" && file.name !in generated }
        ?.filter { localeClass.containsMatchIn(it.readText()) }
        ?.forEach { it.delete() }
}
