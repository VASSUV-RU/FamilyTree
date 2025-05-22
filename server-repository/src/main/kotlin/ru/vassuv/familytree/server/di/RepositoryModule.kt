package ru.vassuv.familytree.server.di

import org.koin.dsl.module
import ru.vassuv.familytree.server.db.DatabaseFactoryInstaller
import ru.vassuv.familytree.server.repository.ExposedFamilyRepository
import ru.vassuv.familytree.server.repository.ExposedPersonRepository
import ru.vassuv.familytree.server.repository.ExposedUserRepository
import ru.vassuv.familytree.server.repository.FamilyRepository
import ru.vassuv.familytree.server.repository.PersonRepository
import ru.vassuv.familytree.server.repository.UserRepository

val repositoryModule = module {
    single { DatabaseFactoryInstaller() }
    single<UserRepository> { ExposedUserRepository() }
    single<FamilyRepository> { ExposedFamilyRepository() }
    single<PersonRepository> { ExposedPersonRepository() }
}