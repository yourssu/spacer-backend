package com.yourssu.spacer.spacehub.business.domain.file

import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service

@Service
class FileService(
    private val fileProcessor: FileProcessor,
) {

    companion object {
        private const val FILE_PROTOCOL_PREFIX = "file:"
    }

    fun read(storeName: String): Resource {
        val storePath = fileProcessor.getFileStorePath(storeName)
        val file = UrlResource(FILE_PROTOCOL_PREFIX + storePath)
        validateFile(file, storeName)

        return file
    }

    private fun validateFile(file: UrlResource, storeName: String) {
        if (!file.exists() || !file.isReadable) {
            throw ReadFailureException("파일[$storeName]이 존재하지 않거나 읽을 수 없습니다.")
        }
    }
}
