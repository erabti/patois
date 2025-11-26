package com.erabti.patois.models

abstract class BaseAppStrings {
    abstract val locale: LocalizationConfig
}

abstract class BaseAppStringsCompanion<StringsT : BaseAppStrings, EnumT : AppLocaleEnum<StringsT>> {
    abstract val supportedLocales: List<EnumT>
}
