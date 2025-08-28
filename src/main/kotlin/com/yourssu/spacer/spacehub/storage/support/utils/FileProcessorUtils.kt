package com.yourssu.spacer.spacehub.storage.support.utils

import com.yourssu.spacer.spacehub.storage.support.exception.UnsupportedFileExtensionException
import java.util.*

object FileProcessorUtils {

    private val EXTENSION = listOf("jpg", "jpeg", "png")
    private const val EXTENSION_SEPARATOR = "."

    fun createStoreFileName(originalFilename: String): String {
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
}