package com.app.contactos.repository

import com.app.contactos.model.Contacto
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface ContactoRepository : JpaRepository<Contacto, Long>, JpaSpecificationExecutor<Contacto> {
    fun existsByCorreo(correo: String): Boolean
    fun existsByCorreoAndIdNot(correo: String, id: Long): Boolean
}