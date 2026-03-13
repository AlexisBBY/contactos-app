package com.app.contactos.model

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "contactos")
data class Contacto(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 100)
    val nombre: String,

    @Column(nullable = false, length = 100)
    val apellidos: String,

    @Column(nullable = false, unique = true, length = 150)
    val correo: String,

    @Column(nullable = false, length = 10)
    val telefono: String,

    @Column(name = "codigo_postal", nullable = false, length = 10)
    val codigoPostal: String,

    @Column(name = "fecha_nacimiento", nullable = false)
    val fechaNacimiento: LocalDate,

    @Column(name = "foto_nombre", length = 255)
    val fotoNombre: String? = null
)