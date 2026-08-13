package com.example.ms_auth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
class MsAuthApplication

fun main(args: Array<String>) {
	runApplication<MsAuthApplication>(*args)
}
