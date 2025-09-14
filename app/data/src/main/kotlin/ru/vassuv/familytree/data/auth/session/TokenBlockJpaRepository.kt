package ru.vassuv.familytree.data.auth.session

import org.springframework.data.jpa.repository.JpaRepository

interface TokenBlockJpaRepository : JpaRepository<TokenBlockEntity, String>

