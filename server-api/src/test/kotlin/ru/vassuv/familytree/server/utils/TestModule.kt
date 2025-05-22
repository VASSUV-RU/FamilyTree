package ru.vassuv.familytree.server.utils

import io.ktor.server.application.Application
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import ru.vassuv.familytree.server.RouterInstaller
import ru.vassuv.familytree.server.config.JsonInstaller
import ru.vassuv.familytree.server.config.StatusPagesInstaller
import kotlin.getValue

internal fun Application.testModule() {
    module {
        // Настройка json сериализации
        val jsonInstaller: JsonInstaller by inject()
        jsonInstaller(this@testModule)

        // Настройка автоматической конвертации исключений в соответсвующие ошибки
        val statusPagesInstaller: StatusPagesInstaller by inject()
        statusPagesInstaller(this@testModule)

        // Настройка роутера
        val routerInstaller: RouterInstaller by inject()
        routerInstaller(this@testModule)
    }
}
