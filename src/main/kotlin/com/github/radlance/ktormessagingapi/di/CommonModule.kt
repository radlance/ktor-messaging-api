package com.github.radlance.ktormessagingapi.di

import com.github.radlance.ktormessagingapi.repository.AuthRepository
import com.github.radlance.ktormessagingapi.security.hashing.HashingService
import com.github.radlance.ktormessagingapi.security.hashing.SHA256HashingService
import com.github.radlance.ktormessagingapi.security.token.TokenConfig
import com.github.radlance.ktormessagingapi.security.token.TokenService
import com.github.radlance.ktormessagingapi.service.AuthService
import io.ktor.server.application.*
import io.ktor.server.config.*
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val commonModule = module {
    singleOf(::SHA256HashingService) bind HashingService::class
    single { AuthRepository() }
    single { TokenService(get()) }
}

fun Application.applicationScopedModule() = module {
    single {
        AuthService(
            authRepository = get(),
            hashingService = get(),
            tokenService = get(),
            jwtExpiration = environment.config.property("jwt.expiration").getAs(),
            refreshExpiration = environment.config.property("jwt.refresh-expiration").getAs(),
        )
    }

    single {
        TokenConfig(
            issuer = environment.config.property("jwt.issuer").getString(),
            audience = environment.config.property("jwt.audience").getString(),
            secret = environment.config.property("jwt.secret").getString()
        )
    }
}