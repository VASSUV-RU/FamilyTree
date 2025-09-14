package ru.vassuv.familytree.data.auth.session

import org.springframework.data.jpa.repository.JpaRepository

interface SessionJpaRepository : JpaRepository<SessionEntity, String>

