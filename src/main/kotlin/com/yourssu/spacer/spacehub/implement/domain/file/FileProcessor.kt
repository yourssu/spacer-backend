package com.yourssu.spacer.spacehub.implement.domain.file

import org.springframework.web.multipart.MultipartFile

interface FileProcessor {

    fun upload(multipartFile: MultipartFile): String
    fun uploadAll(multipartFiles: List<MultipartFile>): List<String>
    fun getFileStorePath(storeName: String): String
    fun getDefaultOrganizationImageUrl(): String
}
