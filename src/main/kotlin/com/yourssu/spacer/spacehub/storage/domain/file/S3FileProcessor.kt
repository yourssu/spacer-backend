package com.yourssu.spacer.spacehub.storage.domain.file

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.yourssu.spacer.spacehub.implement.domain.file.FileProcessor
import com.yourssu.spacer.spacehub.storage.support.exception.InvalidFileException
import com.yourssu.spacer.spacehub.storage.support.utils.FileProcessorUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
@Profile("prod")
class S3FileProcessor(
    private val amazonS3: AmazonS3,

    @Value("\${cloud.aws.s3.bucket}")
    private val bucket: String,

    @Value("\${file.default-imag-name.organization}")
    private val defaultOrganizationImageName: String,

) : FileProcessor {

    override fun upload(multipartFile: MultipartFile): String {
        if (multipartFile.isEmpty) throw InvalidFileException("파일이 비어있습니다.")
        val originalFileName = multipartFile.originalFilename ?: throw InvalidFileException("파일 이름이 비어있습니다.")
        val storeFileName = FileProcessorUtils.createStoreFileName(originalFileName)

        val metadata = ObjectMetadata().apply {
            contentLength = multipartFile.size
            contentType = multipartFile.contentType
        }
        amazonS3.putObject(bucket, storeFileName, multipartFile.inputStream, metadata)
        return amazonS3.getUrl(bucket, storeFileName).toString()
    }

    override fun uploadAll(multipartFiles: List<MultipartFile>): List<String> =
        multipartFiles.map { upload(it) }

    override fun getFileStorePath(storeName: String): String =
        amazonS3.getUrl(bucket, storeName).toString()

    override fun getDefaultOrganizationImageUrl(): String =
        amazonS3.getUrl(bucket, defaultOrganizationImageName).toString()
}
