package io.github.erabti.patois.ktor

import io.github.erabti.patois.ktor.models.GenericLocaleResolver
import io.github.erabti.patois.ktor.models.PatoisPluginConfiguration
import io.github.erabti.patois.ktor.models.Resolver
import io.github.erabti.patois.models.BaseAppStrings
import io.github.erabti.patois.models.LocaleResolver
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.*

fun getResolverAttributeKey(resolver: GenericLocaleResolver) =
    AttributeKey<Resolver>("patois_resolver_${resolver.hashCode()}")

fun Application.installPatoisPlugin(
    resolver: GenericLocaleResolver,
    block: PatoisPluginConfiguration.() -> Unit = {},
) {
    pluginOrNull(PatoisPlugin) ?: install(PatoisPlugin)
    val attribute = getResolverAttributeKey(resolver)
    attributes.computeIfAbsent(attribute) {
        val configuration = PatoisPluginConfiguration().apply(block)

        Resolver(
            localeResolver = resolver, config = configuration
        )
    }
}


inline fun <reified StringsT : BaseAppStrings> ApplicationCall.strings(resolver: LocaleResolver<StringsT, *>): StringsT {
    val attribute = getResolverAttributeKey(resolver)
    val resolver =
        application.attributes.getOrNull(attribute) ?: error("Patois plugin is not installed with the given resolver")

    return resolver.resolve<StringsT>(this)
}

inline fun <reified StringsT : BaseAppStrings> RoutingContext.strings(resolver: LocaleResolver<StringsT, *>): StringsT {
    return call.strings<StringsT>(resolver)
}


internal val PatoisPlugin = createApplicationPlugin(name = "Patois") {}
