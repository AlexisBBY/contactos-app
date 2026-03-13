package com.app.contactos.repository

import com.app.contactos.model.Contacto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ContactoRepository : JpaRepository<Contacto, Long> {

    @Query("""
        SELECT c FROM Contacto c
        WHERE (:nombre IS NULL OR LOWER(CONCAT(c.nombre, ' ', c.apellidos)) LIKE LOWER(CONCAT('%', :nombre, '%')))
        AND (:correo IS NULL OR LOWER(c.correo) LIKE LOWER(CONCAT('%', :correo, '%')))
        AND (:telefono IS NULL OR c.telefono LIKE CONCAT('%', :telefono, '%'))
        AND (:codigoPostal IS NULL OR c.codigoPostal LIKE CONCAT('%', :codigoPostal, '%'))
        AND (:fechaNacimiento IS NULL OR c.fechaNacimiento = :fechaNacimiento)
    """)
    fun buscarConFiltros(
        @Param("nombre") nombre: String?,
        @Param("correo") correo: String?,
        @Param("telefono") telefono: String?,
        @Param("codigoPostal") codigoPostal: String?,
        @Param("fechaNacimiento") fechaNacimiento: LocalDate?,
        pageable: Pageable
    ): Page<Contacto>

    fun existsByCorreo(correo: String): Boolean
    fun existsByCorreoAndIdNot(correo: String, id: Long): Boolean
}