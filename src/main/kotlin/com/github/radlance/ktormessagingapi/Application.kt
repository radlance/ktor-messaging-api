package com.github.radlance.ktormessagingapi

import com.github.radlance.ktormessagingapi.plugins.configureDatabases
import com.github.radlance.ktormessagingapi.plugins.configureFrameworks
import com.github.radlance.ktormessagingapi.plugins.configureHTTP
import com.github.radlance.ktormessagingapi.plugins.configureMonitoring
import com.github.radlance.ktormessagingapi.plugins.configureRouting
import com.github.radlance.ktormessagingapi.plugins.configureSecurity
import com.github.radlance.ktormessagingapi.plugins.configureSerialization
import com.github.radlance.ktormessagingapi.plugins.configureSockets
import com.github.radlance.ktormessagingapi.plugins.configureValidation
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureFrameworks()
    configureValidation()
    configureHTTP()
    configureSerialization()
    configureDatabases()
    configureMonitoring()
    configureSockets()
    configureSecurity()
    configureRouting()
}
