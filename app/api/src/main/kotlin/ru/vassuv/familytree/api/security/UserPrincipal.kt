package ru.vassuv.familytree.api.security

data class UserPrincipal(
    val userId: Long,
    val jti: String,
    val activeFamilyId: Long? = null,
    val scopes: String? = null,
)

