package com.yourssu.spacer.spacehub.business.domain.file

class UnsupportedFileExtensionException(
    override val message: String
) : RuntimeException(message)
