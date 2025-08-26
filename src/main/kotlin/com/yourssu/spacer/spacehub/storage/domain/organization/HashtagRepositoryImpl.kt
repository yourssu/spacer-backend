package com.yourssu.spacer.spacehub.storage.domain.organization

import com.yourssu.spacer.spacehub.business.domain.organization.Hashtag
import com.yourssu.spacer.spacehub.business.domain.organization.HashtagRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class HashtagRepositoryImpl(
    private val jpaHashtagRepository: JpaHashtagRepository
) : HashtagRepository {

    override fun save(hashtag: Hashtag): Hashtag {
        return jpaHashtagRepository.save(HashtagEntity.from(hashtag)).toDomain()
    }

    override fun findById(id: Long): Hashtag? {
        return jpaHashtagRepository.findByIdOrNull(id)?.toDomain()
    }

    override fun findByName(name: String): Hashtag? {
        return jpaHashtagRepository.findByName(name)?.toDomain()
    }
}
