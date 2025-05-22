package ru.vassuv.familytree.server.di

import org.koin.dsl.module
import ru.vassuv.familytree.server.services.FamilyService
import ru.vassuv.familytree.server.services.PersonService
import ru.vassuv.familytree.server.services.UserService

val serviceModule = module {
    single { UserService(get()) }
    single { PersonService(get()) }
    single { FamilyService(get()) }
}