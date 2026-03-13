package com.app.contactos.dto

import java.time.LocalDate

data class ContactoResponseDto(
    val id: Long,
    val nombre: String,
    val apellidos: String,
    val correo: String,
    val telefono: String,
    val codigoPostal: String,
    val fechaNacimiento: LocalDate,
    val fotoUrl: String?
)