package io.github.erabti.patois.ktor.models

import io.github.erabti.patois.models.BaseAppStrings
import io.github.erabti.patois.models.LocaleResolver
import io.ktor.server.application.*

data class Resolver(
    val localeResolver: LocaleResolver<*, *>,
    val config: PatoisPluginConfiguration,
) {
    inline fun <reified StringsT : BaseAppStrings> resolve(call: ApplicationCall): StringsT {
        val locale = config.localeExtractor.extract(call)
        val strings = localeResolver.resolve(locale).toStrings()
        if (strings !is StringsT) {
            error("Resolved strings are not of the expected type")
        }

        return strings
    }
}