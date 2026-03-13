package com.app.contactos

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ContactosApplication

fun main(args: Array<String>) {
    runApplication<ContactosApplication>(*args)
}