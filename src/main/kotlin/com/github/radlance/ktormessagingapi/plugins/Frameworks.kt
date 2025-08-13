package com.github.radlance.ktormessagingapi.plugins

import com.github.radlance.ktormessagingapi.di.authModule
import com.github.radlance.ktormessagingapi.di.chatsModule
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks() {
    install(Koin) {
        slf4jLogger()
        modules(authModule, chatsModule)
    }
}