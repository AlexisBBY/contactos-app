package com.app.contactos.controller

import com.app.contactos.dto.ContactoRequestDto
import com.app.contactos.service.ContactoService
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate

@RestController
@RequestMapping("/api/contactos")
class ContactoController(
    private val service: ContactoService,
    @Value("\${app.upload.dir:uploads/}") private val uploadDir: String
) {

    @GetMapping
    fun listar(
        @RequestParam(required = false) nombre: String?,
        @RequestParam(required = false) correo: String?,
        @RequestParam(required = false) telefono: String?,
        @RequestParam(required = false) codigoPostal: String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fechaNacimiento: LocalDate?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") size: Int
    ): ResponseEntity<Map<String, Any>> {
        val pageSize = size.coerceIn(1, 50)
        val pageNum = page.coerceAtLeast(0)
        return ResponseEntity.ok(service.listar(nombre, correo, telefono, codigoPostal, fechaNacimiento, pageNum, pageSize))
    }

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun crear(
        @Valid @ModelAttribute dto: ContactoRequestDto,
        bindingResult: BindingResult,
        @RequestParam(required = false) foto: MultipartFile?
    ): ResponseEntity<Any> {
        if (bindingResult.hasErrors()) {
            val errores = bindingResult.fieldErrors.associate { it.field to it.defaultMessage }
            return ResponseEntity.badRequest().body(mapOf("errores" to errores))
        }
        return try {
            val creado = service.crear(dto, foto)
            ResponseEntity.status(HttpStatus.CREATED).body(creado)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("mensaje" to e.message))
        }
    }

    @PutMapping("/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun actualizar(
        @PathVariable id: Long,
        @Valid @ModelAttribute dto: ContactoRequestDto,
        bindingResult: BindingResult,
        @RequestParam(required = false) foto: MultipartFile?
    ): ResponseEntity<Any> {
        if (bindingResult.hasErrors()) {
            val errores = bindingResult.fieldErrors.associate { it.field to it.defaultMessage }
            return ResponseEntity.badRequest().body(mapOf("errores" to errores))
        }
        return try {
            ResponseEntity.ok(service.actualizar(id, dto, foto))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("mensaje" to e.message))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("mensaje" to e.message))
        }
    }

    @DeleteMapping("/{id}")
    fun eliminar(@PathVariable id: Long): ResponseEntity<Any> {
        return try {
            service.eliminar(id)
            ResponseEntity.ok(mapOf("mensaje" to "Contacto eliminado correctamente"))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("mensaje" to e.message))
        }
    }

    @GetMapping("/foto/{nombreArchivo}")
    fun obtenerFoto(@PathVariable nombreArchivo: String): ResponseEntity<FileSystemResource> {
        // Prevenir path traversal
        val nombre = Paths.get(nombreArchivo).fileName.toString()
        val ruta = Paths.get(uploadDir).resolve(nombre)
        if (!Files.exists(ruta)) return ResponseEntity.notFound().build()
        val contentType = Files.probeContentType(ruta) ?: "application/octet-stream"
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .body(FileSystemResource(ruta))
    }
}