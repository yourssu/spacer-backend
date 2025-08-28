package com.yourssu.spacer.spacehub.storage.domain.file

import com.yourssu.spacer.spacehub.implement.domain.file.FileProcessor
import com.yourssu.spacer.spacehub.storage.support.exception.InvalidFileException
import com.yourssu.spacer.spacehub.storage.support.utils.FileProcessorUtils
import java.io.File
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
@Profile("local", "test")
class LocalFileProcessor(

    @Value("\${file.upload.path}")
    val uploadPath: String,

    @Value("\${file.web.path}")
    val webApiPath: String,

    @Value("\${file.default-imag-name.organization}")
    val defaultOrganizationImageName: String,

) : FileProcessor {

    override fun upload(multipartFile: MultipartFile): String {
        if (multipartFile.isEmpty) throw InvalidFileException("파일이 비어있습니다.")
        val originalFileName = multipartFile.originalFilename ?: throw InvalidFileException("파일 이름이 비어있습니다.")
        val storeFileName = FileProcessorUtils.createStoreFileName(originalFileName)
        val fullPath = uploadPath + storeFileName
        multipartFile.transferTo(File(fullPath))
        return webApiPath + storeFileName
    }

    override fun uploadAll(multipartFiles: List<MultipartFile>): List<String> =
        multipartFiles.map { upload(it) }

    override fun getFileStorePath(storeName: String): String = uploadPath + storeName

    override fun getDefaultOrganizationImageUrl(): String = webApiPath + defaultOrganizationImageName
}
