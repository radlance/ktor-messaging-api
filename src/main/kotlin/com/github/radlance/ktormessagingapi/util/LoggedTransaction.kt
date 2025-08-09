package com.github.radlance.ktormessagingapi.util

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> loggedTransaction(statement: suspend Transaction.() -> T): T {
    return newSuspendedTransaction(Dispatchers.IO) {
        addLogger(StdOutSqlLogger)
        statement()
    }
}