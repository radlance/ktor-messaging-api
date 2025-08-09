package com.github.radlance.ktormessagingapi.di

import com.github.radlance.ktormessagingapi.repository.api.AuthRepository
import com.github.radlance.ktormessagingapi.repository.impl.AuthRepositoryImpl
import com.github.radlance.ktormessagingapi.security.hashing.HashingService
import com.github.radlance.ktormessagingapi.security.hashing.SHA256HashingService
import com.github.radlance.ktormessagingapi.security.token.JwtTokenService
import com.github.radlance.ktormessagingapi.security.token.TokenConfig
import com.github.radlance.ktormessagingapi.security.token.TokenService
import io.ktor.server.application.*
import io.ktor.server.config.*
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val commonModule = module {
    singleOf(::AuthRepositoryImpl) bind AuthRepository::class
    singleOf(::SHA256HashingService) bind HashingService::class
    singleOf(::JwtTokenService) bind TokenService::class
}

fun Application.applicationScopedModule() = module {
    single<TokenConfig> {
        TokenConfig(
            issuer = environment.config.property("jwt.issuer").getString(),
            audience = environment.config.property("jwt.audience").getString(),
            expiresIn = environment.config.property("jwt.expiration").getAs(),
            secret = environment.config.property("jwt.secret").getString()
        )
    }
}