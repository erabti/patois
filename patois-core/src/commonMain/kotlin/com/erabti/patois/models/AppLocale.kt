package com.erabti.patois.models

data class AppLocale(
    val languageCode: String,
    val countryCode: String? = null,
    val scriptCode: String? = null,
) {
    companion object {
        val ENGLISH_US = AppLocale("en", "US")
        val ENGLISH_GB = AppLocale("en", "GB")
        val FRENCH_FR = AppLocale("fr", "FR")
        val SPANISH_ES = AppLocale("es", "ES")
        val ARABIC_EG = AppLocale("ar", "EG")

        fun fromLanguageTag(tag: String): AppLocale {
            val parts = tag.split("-", "_")
            val size = parts.size

            return when {
                size == 1 -> AppLocale(parts[0])
                size == 2 -> AppLocale(parts[0], parts[1])
                size > 2 -> AppLocale(parts[0], scriptCode = parts[1], countryCode = parts[2])
                else -> throw IllegalArgumentException("Invalid language tag: $tag")
            }
        }
    }

    val languageTag: String by lazy {
        toLanguageTag()
    }


    private fun toLanguageTag(delimiter: String = "-"): String {
        val parts = mutableListOf<String>()
        parts.add(languageCode)
        countryCode?.let { parts.add(it) }
        scriptCode?.let { parts.add(it) }
        return parts.joinToString(delimiter)
    }
}