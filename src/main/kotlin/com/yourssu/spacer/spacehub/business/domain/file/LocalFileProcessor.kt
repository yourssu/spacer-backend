package com.yourssu.spacer.spacehub.business.domain.file

import java.io.File
import java.io.IOException
import java.util.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class LocalFileProcessor(

    @Value("\${file.upload.path}")
    val uploadPath: String,

    @Value("\${file.web.path}")
    val webApiPath: String,

    @Value("\${file.default-imag-name.organization}")
    val defaultOrganizationImageName: String,

) : FileProcessor {

    companion object {
        private val EXTENSION = listOf("jpg", "jpeg", "png")
        private const val EXTENSION_SEPARATOR = "."
    }

    override fun upload(multipartFile: MultipartFile): String {
        if (multipartFile.isEmpty) {
            throw InvalidFileException("파일이 비어있습니다.")
        }
        val originalFileName = multipartFile.originalFilename ?: throw InvalidFileException("파일 이름이 비어있습니다.")
        val storeFileName = createStoreFileName(originalFileName)
        val uploadPath = uploadPath + storeFileName
        upload(multipartFile, uploadPath)

        return webApiPath + storeFileName
    }

    private fun upload(
        multipartFile: MultipartFile,
        uploadPath: String,
    ) {
        try {
            multipartFile.transferTo(File(uploadPath))
        } catch (ex: IOException) {
            throw StoreFailureException("파일 저장에 실패했습니다.", ex)
        }
    }

    override fun uploadAll(multipartFiles: List<MultipartFile>): List<String> = multipartFiles.map { upload(it) }

    private fun createStoreFileName(originalFilename: String): String {
        val extension = extractExtension(originalFilename)
        val uuid = UUID.randomUUID().toString()

        return "$uuid$EXTENSION_SEPARATOR$extension"
    }

    private fun extractExtension(originalFilename: String): String {
        val extension = originalFilename.substring(originalFilename.lastIndexOf(EXTENSION_SEPARATOR) + 1)
        if (!EXTENSION.contains(extension.lowercase())) {
            throw UnsupportedFileExtensionException("지원하지 않는 확장자입니다. : $extension")
        }

        return extension
    }

    override fun getFileStorePath(storeName: String): String = uploadPath + storeName

    override fun getDefaultOrganizationImageUrl(): String = webApiPath + defaultOrganizationImageName
}
