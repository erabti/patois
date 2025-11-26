package com.erabti.patois.ktor.models

import com.erabti.patois.models.AppLocaleEnum
import com.erabti.patois.models.BaseAppStrings
import com.erabti.patois.models.LocaleResolver

internal typealias GenericLocaleResolver = LocaleResolver<out BaseAppStrings, out AppLocaleEnum<out BaseAppStrings>>
