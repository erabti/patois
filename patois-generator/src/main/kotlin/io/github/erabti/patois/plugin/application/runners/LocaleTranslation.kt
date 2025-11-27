package io.github.erabti.patois.plugin.application.runners

import io.github.erabti.patois.models.LocalizationConfig
import io.github.erabti.patois.plugin.models.TranslationNode

data class LocaleTranslation(
    val locale: LocalizationConfig,
    val fileName: String,
    val nodes: List<TranslationNode>,
)