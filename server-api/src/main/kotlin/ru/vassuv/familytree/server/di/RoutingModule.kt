package ru.vassuv.familytree.server.di

import org.koin.dsl.module
import ru.vassuv.familytree.server.RouterInstaller
import ru.vassuv.familytree.server.config.JsonInstaller
import ru.vassuv.familytree.server.config.StatusPagesInstaller

val routingModule = module {
    single { RouterInstaller(get(), get()) }
}