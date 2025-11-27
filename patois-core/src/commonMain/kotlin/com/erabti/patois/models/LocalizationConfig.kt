package com.erabti.patois.models


interface AppLocaleEnum<StringsT : BaseAppStrings> {
    fun toStrings(): StringsT

    @Suppress("UNCHECKED_CAST")
    fun <R> toStrings(): R = toStrings() as R

    val config: LocalizationConfig

    val tag: String
        get() = config.tag
}


interface LocaleResolver<StringsT : BaseAppStrings, EnumT : AppLocaleEnum<StringsT>> {
    val defaultLocale: EnumT

    val supportedLocales: List<EnumT>

    fun resolve(tag: String? = null): EnumT

    fun resolveToStrings(tag: String? = null) = resolve(tag).toStrings()

    operator fun invoke(tag: String? = null): EnumT = resolve(tag)
}

fun <T> Map<LocalizationConfig, T>.findNearestLocale(
    target: String,
    fallback: T,
): T {
    val target = LocalizationConfig.fromLanguageTagOrNull(target) ?: return fallback

    // Exact match
    this[target]?.let { return it }

    // Match language and country
    this.entries.firstOrNull { it.key.languageCode == target.languageCode && it.key.countryCode == target.countryCode }
        ?.let { return it.value }

    // Match language only
    this.entries.firstOrNull { it.key.languageCode == target.languageCode }?.let { return it.value }

    // Fallback
    return fallback
}

fun List<LocalizationConfig>.findNearestLocale(
    target: String,
    fallback: LocalizationConfig,
): LocalizationConfig {
    val target = LocalizationConfig.fromLanguageTag(target, fallback)

    // Exact match
    this.firstOrNull { it == target }?.let { return it }

    // Match language and country
    this.firstOrNull { it.languageCode == target.languageCode && it.countryCode == target.countryCode }
        ?.let { return it }

    // Match language only
    this.firstOrNull { it.languageCode == target.languageCode }?.let { return it }

    // Fallback
    return fallback
}


class LocalizationConfig(
    languageCode: String,
    countryCode: String? = null,
    scriptCode: String? = null,
) {
    val languageCode: String = languageCode.uppercase()
    val countryCode: String? = countryCode?.uppercase()
    val scriptCode: String? = scriptCode?.uppercase()

    companion object {
        val DEFAULT = LocalizationConfigDefaults
        fun fromLanguageTagOrNull(tag: String): LocalizationConfig? {
            val parts = tag.split("-", "_", ".", " ").map { it.trim().uppercase() }
            val size = parts.size

            return when {
                size == 1 -> LocalizationConfig(parts[0])
                size == 2 -> LocalizationConfig(parts[0], parts[1])
                size > 2 -> LocalizationConfig(parts[0], scriptCode = parts[1], countryCode = parts[2])
                else -> null
            }
        }

        fun fromLanguageTag(tag: String, fallback: LocalizationConfig? = null): LocalizationConfig {
            return fromLanguageTagOrNull(tag) ?: fallback
            ?: throw IllegalArgumentException("Invalid language tag: $tag")
        }
    }

    val tag: String by lazy {
        toTag()
    }

    fun toTag(delimiter: String = "-"): String {
        val parts = mutableListOf<String>()
        parts.add(languageCode)
        countryCode?.let { parts.add(it) }
        scriptCode?.let { parts.add(it) }
        return parts.joinToString(delimiter)
    }


    // equality override
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LocalizationConfig) return false

        if (languageCode != other.languageCode) return false
        if (countryCode != other.countryCode) return false
        if (scriptCode != other.scriptCode) return false

        return true
    }

    override fun hashCode(): Int {
        var result = languageCode.hashCode()
        result = 31 * result + (countryCode?.hashCode() ?: 0)
        result = 31 * result + (scriptCode?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        val countryCode = this.countryCode?.let { ", countryCode=$it" } ?: ""
        val scriptCode = this.scriptCode?.let { ", scriptCode=$it" } ?: ""

        return "LocalizationConfig(languageCode=$languageCode$countryCode$scriptCode)"
    }
}