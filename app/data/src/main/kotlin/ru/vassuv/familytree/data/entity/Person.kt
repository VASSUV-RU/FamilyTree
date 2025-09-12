package ru.vassuv.familytree.data.entity

import jakarta.persistence.*

@Entity
@Table(name = "persons")
class Person(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var firstName: String = "",

    @Column(nullable = false)
    var lastName: String = "",
)
