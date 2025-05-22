package ru.vassuv.familytree.server.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database

class DatabaseFactoryInstaller(): () -> Unit {
    override fun invoke() {
        DatabaseFactory.init()
    }
}

internal object DatabaseFactory {
    fun init(url: String = dbUrl, user: String = dbUser, pass: String = dbPass) {
        val config = HikariConfig().apply {
            jdbcUrl = url
            driverClassName = "org.postgresql.Driver"
            username = user
            password = pass
            maximumPoolSize = 5
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }

        val dataSource = HikariDataSource(config)

        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .load()
            .migrate()

        Database.connect(dataSource)
//
//        transaction {
//            SchemaUtils.create(UserTable)
//        }
    }
}