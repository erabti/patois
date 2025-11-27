package io.github.erabti.patois.ktor.models

import io.github.erabti.patois.models.AppLocaleEnum
import io.github.erabti.patois.models.BaseAppStrings
import io.github.erabti.patois.models.LocaleResolver

internal typealias GenericLocaleResolver = LocaleResolver<out BaseAppStrings, out AppLocaleEnum<out BaseAppStrings>>
