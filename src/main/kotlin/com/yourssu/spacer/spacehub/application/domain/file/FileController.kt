package com.yourssu.spacer.spacehub.application.domain.file

import com.yourssu.spacer.spacehub.business.domain.file.FileService
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/files")
class FileController(
    private val fileService: FileService,
) {

    @GetMapping("/{storeName}")
    fun read(@PathVariable storeName: String): ResponseEntity<Resource> {
        val resource: Resource = fileService.read(storeName)
        val headers = HttpHeaders()
        headers.contentType = MediaType.IMAGE_JPEG

        return ResponseEntity(resource, headers, HttpStatus.OK)
    }
}
