package com.yourssu.spacer.spacehub.storage.domain.slack

import org.springframework.data.jpa.repository.JpaRepository

interface JpaSlackWorkspaceLinkRepository : JpaRepository<SlackWorkspaceLinkEntity, String>
