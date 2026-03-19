package com.app.contactos.service

import com.app.contactos.dto.ContactoRequestDto
import com.app.contactos.dto.ContactoResponseDto
import com.app.contactos.model.Contacto
import com.app.contactos.repository.ContactoRepository
import jakarta.persistence.criteria.Predicate
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDate
import java.util.UUID

@Service
class ContactoService(
    private val repo: ContactoRepository,
    @Value("\${app.upload.dir:uploads/}") private val uploadDir: String
) {

    private val allowedImageTypes = setOf("image/jpeg", "image/png", "image/gif", "image/webp")
    private val maxFileSizeBytes = 2 * 1024 * 1024L // 2MB

    fun listar(
        nombre: String?, correo: String?, telefono: String?,
        codigoPostal: String?, fechaNacimiento: LocalDate?,
        page: Int, size: Int
    ): Map<String, Any> {
        val pageable = PageRequest.of(page, size, Sort.by("apellidos").ascending())

        val spec = Specification<Contacto> { root, _, cb ->
            val predicates = mutableListOf<Predicate>()

            nombre?.takeIf { it.isNotBlank() }?.let {
                val nombreCompleto = cb.lower(
                    cb.concat(
                        cb.concat(root.get<String>("nombre"), " "),
                        root.get<String>("apellidos")
                    )
                )
                predicates += cb.like(nombreCompleto, "%${it.lowercase()}%")
            }

            correo?.takeIf { it.isNotBlank() }?.let {
                predicates += cb.like(
                    cb.lower(root.get("correo")),
                    "%${it.lowercase()}%"
                )
            }

            telefono?.takeIf { it.isNotBlank() }?.let {
                predicates += cb.like(root.get("telefono"), "%$it%")
            }

            codigoPostal?.takeIf { it.isNotBlank() }?.let {
                predicates += cb.like(root.get("codigoPostal"), "%$it%")
            }

            fechaNacimiento?.let {
                predicates += cb.equal(root.get<LocalDate>("fechaNacimiento"), it)
            }

            cb.and(*predicates.toTypedArray())
        }

        val resultado: Page<Contacto> = repo.findAll(spec, pageable)

        return mapOf(
            "content" to resultado.content.map { toDto(it) },
            "totalElements" to resultado.totalElements,
            "totalPages" to resultado.totalPages,
            "currentPage" to resultado.number,
            "pageSize" to resultado.size
        )
    }

    @Transactional
    fun crear(dto: ContactoRequestDto, foto: MultipartFile?): ContactoResponseDto {
        if (repo.existsByCorreo(dto.correo.lowercase())) {
            throw IllegalArgumentException("Ya existe un contacto con ese correo electrónico")
        }

        val fotoNombre = foto?.let { guardarFoto(it) }

        val contacto = Contacto(
            nombre = sanitizar(dto.nombre),
            apellidos = sanitizar(dto.apellidos),
            correo = dto.correo.lowercase().trim(),
            telefono = dto.telefono.trim(),
            codigoPostal = dto.codigoPostal.trim(),
            fechaNacimiento = dto.fechaNacimiento,
            fotoNombre = fotoNombre
        )

        return toDto(repo.save(contacto))
    }

    @Transactional
    fun actualizar(id: Long, dto: ContactoRequestDto, foto: MultipartFile?): ContactoResponseDto {
        val existente = repo.findById(id).orElseThrow { NoSuchElementException("Contacto no encontrado") }

        if (repo.existsByCorreoAndIdNot(dto.correo.lowercase(), id)) {
            throw IllegalArgumentException("Ya existe un contacto con ese correo electrónico")
        }

        val fotoNombre = foto?.let { guardarFoto(it) } ?: existente.fotoNombre

        val actualizado = existente.copy(
            nombre = sanitizar(dto.nombre),
            apellidos = sanitizar(dto.apellidos),
            correo = dto.correo.lowercase().trim(),
            telefono = dto.telefono.trim(),
            codigoPostal = dto.codigoPostal.trim(),
            fechaNacimiento = dto.fechaNacimiento,
            fotoNombre = fotoNombre
        )

        return toDto(repo.save(actualizado))
    }

    @Transactional
    fun eliminar(id: Long) {
        if (!repo.existsById(id)) {
            throw NoSuchElementException("Contacto no encontrado")
        }
        repo.deleteById(id)
    }

    private fun guardarFoto(foto: MultipartFile): String {
        if (foto.isEmpty) throw IllegalArgumentException("El archivo está vacío")
        if (foto.size > maxFileSizeBytes) throw IllegalArgumentException("La imagen no puede superar 2MB")

        val contentType = foto.contentType ?: throw IllegalArgumentException("Tipo de archivo no válido")
        if (contentType !in allowedImageTypes) {
            throw IllegalArgumentException("Solo se permiten imágenes JPG, PNG, GIF o WEBP")
        }

        val extension = when (contentType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/gif" -> "gif"
            "image/webp" -> "webp"
            else -> throw IllegalArgumentException("Tipo de imagen no soportado")
        }

        val nombreArchivo = "${UUID.randomUUID()}.$extension"
        val directorio = Paths.get(uploadDir)
        Files.createDirectories(directorio)
        Files.copy(foto.inputStream, directorio.resolve(nombreArchivo), StandardCopyOption.REPLACE_EXISTING)

        return nombreArchivo
    }

    private fun sanitizar(texto: String): String {
        return texto.trim()
            .replace(Regex("[<>\"';&]"), "")
            .take(100)
    }

    private fun toDto(c: Contacto) = ContactoResponseDto(
        id = c.id,
        nombre = c.nombre,
        apellidos = c.apellidos,
        correo = c.correo,
        telefono = c.telefono,
        codigoPostal = c.codigoPostal,
        fechaNacimiento = c.fechaNacimiento,
        fotoUrl = c.fotoNombre?.let { "/api/contactos/foto/$it" }
    )
}