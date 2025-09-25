package com.yourssu.spacer.spacehub.storage.domain.slack

import com.yourssu.spacer.spacehub.implement.domain.slack.SlackWorkspaceLink
import com.yourssu.spacer.spacehub.implement.domain.slack.SlackWorkspaceLinkRepository
import org.springframework.stereotype.Repository

@Repository
class SlackWorkspaceLinkRepositoryImpl(
    private val jpaSlackWorkspaceLinkRepository: JpaSlackWorkspaceLinkRepository
) : SlackWorkspaceLinkRepository {

    override fun save(slackWorkspaceLink: SlackWorkspaceLink): SlackWorkspaceLink {
        return jpaSlackWorkspaceLinkRepository.save(SlackWorkspaceLinkEntity.from(slackWorkspaceLink)).toDomain()
    }

    override fun findByTeamId(teamId: String): SlackWorkspaceLink? {
        return jpaSlackWorkspaceLinkRepository.findByTeamId(teamId)?.toDomain()
    }

    override fun delete(slackWorkspaceLink: SlackWorkspaceLink) {
        jpaSlackWorkspaceLinkRepository.deleteById(slackWorkspaceLink.teamId)
    }
}
