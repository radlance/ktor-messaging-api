package com.github.radlance.ktormessagingapi.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTVerificationException
import com.github.radlance.ktormessagingapi.domain.auth.LoginUser
import com.github.radlance.ktormessagingapi.domain.auth.RefreshToken
import com.github.radlance.ktormessagingapi.domain.auth.RegisterUser
import com.github.radlance.ktormessagingapi.domain.auth.Tokens
import com.github.radlance.ktormessagingapi.domain.auth.User
import com.github.radlance.ktormessagingapi.repository.api.AuthRepository
import com.github.radlance.ktormessagingapi.security.hashing.HashingService
import com.github.radlance.ktormessagingapi.security.hashing.SaltedHash
import com.github.radlance.ktormessagingapi.security.token.TokenService
import com.github.radlance.ktormessagingapi.security.token.TokenType
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.register(
    authRepository: AuthRepository,
    hashingService: HashingService,
    tokenService: TokenService
) {
    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterUser>()

            if (authRepository.getUserByEmail(request.email) != null) {
                call.respond(HttpStatusCode.BadRequest, "Email already taken! Input another email")
                return@post
            }

            val saltedHash = hashingService.generateSaltedHash(request.password)

            val user = RegisterUser(
                email = request.email,
                password = saltedHash.hash,
                displayName = request.displayName
            )

            val createdUser: User = authRepository.create(user, saltedHash.salt)
            call.respond(createdUser)
        }

        post("/login") {
            val request = call.receive<LoginUser>()
            val user = authRepository.getUserByEmail(request.email)

            if (user == null) {
                call.respond(HttpStatusCode.BadRequest, "Incorrect email or password")
                return@post
            }

            val isValidPassword = hashingService.verify(
                value = request.password,
                saltedHash = SaltedHash(
                    hash = user.passwordHash,
                    salt = user.salt
                )
            )
            if (!isValidPassword) {
                call.respond(HttpStatusCode.BadRequest, "Incorrect email or password")
                return@post
            }

            val accessToken = tokenService.generateToken(
                userEmail = user.email,
                tokenType = TokenType.ACCESS_TOKEN.name,
                expirationDate = environment.config.property("jwt.expiration").getAs()
            )

            val refreshToken = tokenService.generateToken(
                userEmail = user.email,
                tokenType = TokenType.REFRESH_TOKEN.name,
                expirationDate = environment.config.property("jwt.refresh-expiration").getAs()
            )


            call.respond(
                status = HttpStatusCode.OK,
                message = Tokens(accessToken = accessToken, refreshToken = refreshToken)
            )
        }

        post("/refresh-token") {
            val request = call.receive<RefreshToken>()
            val decodedJWT = JWT.decode(request.refreshToken)

            val principal = try {
                val tokenType = decodedJWT.getClaim("tokenType").asString()
                if (tokenType != TokenType.REFRESH_TOKEN.name) {
                    throw JWTVerificationException("Invalid token type")
                }

                tokenService.verifyToken(TokenType.REFRESH_TOKEN.name).verify(decodedJWT)
                decodedJWT
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid or expired refresh token")
                return@post
            }

            val userEmail = principal.getClaim("email").asString()

            val newAccessToken = tokenService.generateToken(
                userEmail = userEmail,
                tokenType = TokenType.ACCESS_TOKEN.name,
                expirationDate = environment.config.property("jwt.expiration").getAs()
            )

            val newRefreshToken = tokenService.generateToken(
                userEmail = userEmail,
                tokenType = TokenType.REFRESH_TOKEN.name,
                expirationDate = environment.config.property("jwt.refresh-expiration").getAs()
            )

            call.respond(
                HttpStatusCode.OK,
                Tokens(accessToken = newAccessToken, refreshToken = newRefreshToken)
            )
        }

        authenticate {
            get("/authenticate") {
                val principal = call.principal<JWTPrincipal>()
                val userEmail = principal?.getClaim("email", String::class)
                call.respond(HttpStatusCode.OK, "Your email is $userEmail")
            }
        }
    }
}