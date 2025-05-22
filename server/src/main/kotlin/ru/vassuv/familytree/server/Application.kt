package ru.vassuv.familytree.server

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.application.*
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import ru.vassuv.familytree.server.config.JsonInstaller
import ru.vassuv.familytree.server.config.StatusPagesInstaller
import ru.vassuv.familytree.server.db.DatabaseFactoryInstaller
import ru.vassuv.familytree.server.di.configModule
import ru.vassuv.familytree.server.di.repositoryModule
import ru.vassuv.familytree.server.di.routingModule
import ru.vassuv.familytree.server.di.serviceModule

fun main() {
    embeddedServer(
        factory = Netty,
        port = 8081,
        host = "0.0.0.0",
        module = Application::module
    )
        .start(wait = true)
}

internal fun Application.module(testing: Boolean = false) {
    // Конфигурация DI
    install(Koin) {
        modules(configModule, repositoryModule, serviceModule, routingModule)
    }

    // Настройка json сериализации
    val jsonInstaller: JsonInstaller by inject()
    jsonInstaller(this)

    // Настройка автоматической конвертации исключений в соответсвующие ошибки
    val statusPagesInstaller: StatusPagesInstaller by inject()
    statusPagesInstaller(this)

    // Настройка базы данных
    if(!testing) {
        val databaseFactoryInstaller by inject<DatabaseFactoryInstaller>()
        databaseFactoryInstaller()
    }

    // Настройка роутера
    val routerInstaller: RouterInstaller by inject()
    routerInstaller(this)

}