package ru.vassuv.familytree.data.auth.audit

import org.springframework.data.jpa.repository.JpaRepository

interface AuthAuditJpaRepository : JpaRepository<AuthAuditEntity, Long>
