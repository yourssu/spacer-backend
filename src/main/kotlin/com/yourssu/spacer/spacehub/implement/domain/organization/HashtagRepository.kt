package com.yourssu.spacer.spacehub.implement.domain.organization

interface HashtagRepository {

    fun save(hashtag: Hashtag): Hashtag
    fun findById(id: Long): Hashtag?
    fun findByName(name: String): Hashtag?
}
