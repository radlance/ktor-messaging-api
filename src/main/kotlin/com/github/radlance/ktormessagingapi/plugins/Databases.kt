package com.github.radlance.ktormessagingapi.plugins

import io.ktor.server.application.*
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.jetbrains.exposed.sql.Database
import java.sql.Connection
import java.sql.DriverManager

fun Application.configureDatabases() {
    connectToPostgres()
}

fun Application.connectToPostgres() {

    val url = environment.config.property("database.url").getString()
    log.info("Connecting to postgres database at $url")
    val user = environment.config.property("database.user").getString()
    val password = environment.config.property("database.password").getString()
    val driver = environment.config.property("database.driver").getString()

    Database.connect(
        url = url,
        user = user,
        password = password,
        driver = driver
    )

    runLiquibaseMigrations(DriverManager.getConnection(url, user, password))
}

private fun Application.runLiquibaseMigrations(connection: Connection) {
    try {
        val database = DatabaseFactory.getInstance()
            .findCorrectDatabaseImplementation(JdbcConnection(connection))

        val liquibase = Liquibase(
            "db/changelog/db.changelog-master.yaml",
            ClassLoaderResourceAccessor(),
            database
        )

        liquibase.update()
        log.info("Liquibase migrations applied successfully")
    } catch (e: Exception) {
        log.error("Liquibase migration failed", e)
        throw e
    }
}