package com.erabti.patois.ktor.models

import io.ktor.http.*
import io.ktor.server.application.*


fun interface PatoisLocaleExtractor {
    fun extract(call: ApplicationCall): String?
}


val DefaultPatoisLocaleExtractor = PatoisLocaleExtractor { call ->
    call.request.headers[HttpHeaders.AcceptLanguage]
}