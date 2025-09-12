package ru.vassuv.familytree.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(
    scanBasePackages = ["ru.vassuv.familytree"]
)
@EnableJpaRepositories(basePackages = ["ru.vassuv.familytree.data.repo"])
@EntityScan(basePackages = ["ru.vassuv.familytree.data.entity"])
class ApiApplication

fun main(args: Array<String>) {
    runApplication<ApiApplication>(*args)
}

