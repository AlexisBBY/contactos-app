package com.app.contactos.dto

import jakarta.validation.constraints.*

data class ContactoRequestDto(

    @field:NotBlank(message = "El nombre es obligatorio")
    @field:Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @field:Pattern(
        regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s'-]+$",
        message = "El nombre solo puede contener letras, espacios, guiones y apóstrofes"
    )
    val nombre: String,

    @field:NotBlank(message = "Los apellidos son obligatorios")
    @field:Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres")
    @field:Pattern(
        regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s'-]+$",
        message = "Los apellidos solo pueden contener letras, espacios, guiones y apóstrofes"
    )
    val apellidos: String,

    @field:NotBlank(message = "El correo es obligatorio")
    @field:Email(message = "El correo electrónico no tiene un formato válido")
    @field:Size(max = 150, message = "El correo no puede superar 150 caracteres")
    val correo: String,

    @field:NotBlank(message = "El teléfono es obligatorio")
    @field:Pattern(
        regexp = "^\\d{10}$",
        message = "El teléfono debe contener exactamente 10 dígitos numéricos"
    )
    val telefono: String,

    @field:NotBlank(message = "El código postal es obligatorio")
    @field:Pattern(
        regexp = "^\\d{4,10}$",
        message = "El código postal debe contener entre 4 y 10 dígitos"
    )
    val codigoPostal: String,

    @field:NotNull(message = "La fecha de nacimiento es obligatoria")
    @field:Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    val fechaNacimiento: java.time.LocalDate
)