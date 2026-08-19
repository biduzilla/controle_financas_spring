package com.example.ms_category

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
class MsCategoryApplication

fun main(args: Array<String>) {
	runApplication<MsCategoryApplication>(*args)
}
