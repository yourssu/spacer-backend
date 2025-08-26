package com.yourssu.spacer.spacehub.business.domain.organization

class OrganizationName(
    val name: String,
) {

    companion object {
        private const val MIN_LENGTH = 1
        private const val MAX_LENGTH = 20
    }

    init {
        validateNotBlank(name)
        validateLength(name)
    }

    private fun validateNotBlank(name: String) {
        if (name.isBlank()) {
            throw InvalidOrganizationNameException("단체 이름이 빈 값입니다.")
        }
    }

    private fun validateLength(name: String) {
        if (name.length !in MIN_LENGTH..MAX_LENGTH) {
            throw InvalidOrganizationNameException("단체 이름은 $MIN_LENGTH~$MAX_LENGTH 글자여야 합니다.")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OrganizationName

        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return "OrganizationName(name='$name')"
    }
}
