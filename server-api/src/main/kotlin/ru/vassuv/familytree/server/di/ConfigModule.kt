package ru.vassuv.familytree.server.di

import org.koin.dsl.module
import ru.vassuv.familytree.server.config.JsonInstaller
import ru.vassuv.familytree.server.config.StatusPagesInstaller

val configModule = module {
    single { StatusPagesInstaller() }
    single { JsonInstaller() }
}