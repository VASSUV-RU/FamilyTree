package ru.vassuv.familytree.api.controller

import ru.vassuv.familytree.service.FamilyService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/people")
class PeopleController(
    private val familyService: FamilyService,
) {
    @GetMapping("/count")
    fun count(): Map<String, Long> = mapOf("count" to familyService.countPeople())
}

