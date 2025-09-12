package ru.vassuv.familytree.data.repo

import ru.vassuv.familytree.data.entity.Person
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PersonRepository : JpaRepository<Person, Long> {

    @Query("SELECT COUNT(u) FROM Person u")
    fun countUsers(): Long
}

