package com.yourssu.spacer.spacehub.storage.domain.slack

import com.yourssu.spacer.spacehub.implement.domain.slack.SlackWorkspaceLink
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.Table

@Entity
@Table(name = "slack_workspace_link")
class SlackWorkspaceLinkEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    val teamId: String,

    @Lob
    @Column(nullable = false)
    val accessToken: String,

    @Column(nullable = true)
    val organizationId: Long?
) {
    companion object {
        fun from(link: SlackWorkspaceLink) = SlackWorkspaceLinkEntity(
            id = link.id,
            teamId = link.teamId,
            accessToken = link.accessToken,
            organizationId = link.organizationId
        )
    }

    fun toDomain() = SlackWorkspaceLink(
        id = this.id,
        teamId = this.teamId,
        accessToken = this.accessToken,
        organizationId = this.organizationId
    )
}
