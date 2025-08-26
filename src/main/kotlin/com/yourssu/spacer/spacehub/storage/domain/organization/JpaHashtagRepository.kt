package com.yourssu.spacer.spacehub.storage.domain.organization

import org.springframework.data.jpa.repository.JpaRepository

interface JpaHashtagRepository : JpaRepository<HashtagEntity, Long> {
    fun findByName(name: String): HashtagEntity?
}
