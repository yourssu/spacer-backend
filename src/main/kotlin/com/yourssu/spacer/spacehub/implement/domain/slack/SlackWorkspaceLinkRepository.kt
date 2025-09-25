package com.yourssu.spacer.spacehub.implement.domain.slack

interface SlackWorkspaceLinkRepository {
    fun save(slackWorkspaceLink: SlackWorkspaceLink): SlackWorkspaceLink
    fun findByTeamId(teamId: String): SlackWorkspaceLink?
    fun delete(slackWorkspaceLink: SlackWorkspaceLink)
}
