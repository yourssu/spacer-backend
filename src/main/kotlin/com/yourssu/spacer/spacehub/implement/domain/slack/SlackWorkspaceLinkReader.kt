package com.yourssu.spacer.spacehub.implement.domain.slack

import com.yourssu.spacer.spacehub.implement.support.exception.SlackWorkspaceLinkNotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class SlackWorkspaceLinkReader(
    private val slackWorkspaceLinkRepository: SlackWorkspaceLinkRepository
) {
    fun getByTeamId(teamId: String): SlackWorkspaceLink {
        return slackWorkspaceLinkRepository.findByTeamId(teamId)
            ?: throw SlackWorkspaceLinkNotFoundException("해당 워크스페이스와 등록된 단체가 없습니다")
    }
}