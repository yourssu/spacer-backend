package com.yourssu.spacer.spacehub.storage.domain.organization

import com.yourssu.spacer.spacehub.business.domain.organization.Hashtag
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "hashtag")
class HashtagEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val name: String,
) {

    companion object {
        fun from(hashtag: Hashtag) = HashtagEntity(
            id = hashtag.id,
            name = hashtag.name,
        )
    }

    fun toDomain() = Hashtag(
        id = id,
        name = name,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HashtagEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "HashtagEntity(id=$id, name='$name')"
    }
}
