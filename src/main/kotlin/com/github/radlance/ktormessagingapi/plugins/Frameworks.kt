package com.github.radlance.ktormessagingapi.plugins

import com.github.radlance.ktormessagingapi.di.applicationScopedModule
import com.github.radlance.ktormessagingapi.di.commonModule
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks() {
    install(Koin) {
        slf4jLogger()
        modules(commonModule, applicationScopedModule())
    }
}