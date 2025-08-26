package com.yourssu.spacer.spacehub.business.domain.file

class InvalidFileException(
    override val message: String
) : RuntimeException(message)
