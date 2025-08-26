package com.yourssu.spacer.spacehub.business.domain.organization

class Hashtag(
    val id: Long? = null,
    val name: String,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Hashtag

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "Hashtag(id=$id, name='$name')"
    }
}
