package com.erabti.patois.plugin.application.runners

import com.erabti.patois.models.LocalizationConfig
import com.erabti.patois.plugin.models.TranslationNode

data class LocaleTranslation(
    val locale: LocalizationConfig,
    val fileName: String,
    val nodes: List<TranslationNode>,
)