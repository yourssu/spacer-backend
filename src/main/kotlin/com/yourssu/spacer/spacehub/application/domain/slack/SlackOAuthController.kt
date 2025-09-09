package com.yourssu.spacer.spacehub.application.domain.slack

import com.slack.api.Slack
import com.yourssu.spacer.spacehub.implement.domain.slack.SlackWorkspaceLink
import com.yourssu.spacer.spacehub.implement.domain.slack.SlackWorkspaceLinkRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class SlackOAuthController(
    private val slackWorkspaceLinkRepository: SlackWorkspaceLinkRepository
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Value("\${slack.client-id}")
    private lateinit var clientId: String

    @Value("\${slack.client-secret}")
    private lateinit var clientSecret: String

    @GetMapping("/slack/oauth/callback")
    fun handleCallback(@RequestParam code: String): String {
        val slack = Slack.getInstance()
        val response = try {
            slack.methods().oauthV2Access { req ->
                req.clientId(clientId).clientSecret(clientSecret).code(code)
            }
        } catch (e: Exception) {
            logger.error("Slack OAuth Error: {}", e.message, e)
            return "Slack 인증 중 오류가 발생했습니다: ${e.message}"
        }

        if (response.isOk) {
            val teamId = response.team.id
            val accessToken = response.accessToken

            // TODO: 현재 앱을 설치하는 사용자를 기반으로 organizationId를 결정해야 합니다.
            // 지금은 임시로 1L을 사용합니다.
            val organizationId = 1L

            // 이미 설치된 워크스페이스인지 확인
            val existingLink = slackWorkspaceLinkRepository.findByTeamId(teamId)

            if (existingLink != null) {
                // 이미 존재하면 토큰만 업데이트
                val updatedLink = SlackWorkspaceLink(
                    id = existingLink.id,
                    teamId = existingLink.teamId,
                    accessToken = accessToken,
                    organizationId = existingLink.organizationId // 기존 organizationId 유지
                )
                slackWorkspaceLinkRepository.save(updatedLink)
                logger.info("Slack 앱 토큰 업데이트 완료: teamId={}", teamId)
            } else {
                // 새로 설치하는 경우
                val newLink = SlackWorkspaceLink(
                    teamId = teamId,
                    accessToken = accessToken,
                    organizationId = organizationId
                )
                slackWorkspaceLinkRepository.save(newLink)
                logger.info("새로운 Slack 앱 설치 완료: teamId={}", teamId)
            }
            return "✅ 예약 봇이 워크스페이스에 성공적으로 설치되었습니다!"
        } else {
            logger.error("Slack API Error: {}", response.error)
            return "설치에 실패했습니다: ${response.error}"
        }
    }
}