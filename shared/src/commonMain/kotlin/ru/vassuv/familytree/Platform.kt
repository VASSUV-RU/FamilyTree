package ru.vassuv.familytree

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform