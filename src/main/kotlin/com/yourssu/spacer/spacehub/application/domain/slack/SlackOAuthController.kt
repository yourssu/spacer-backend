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
            val installerUserId = response.authedUser.id

            val existingLink = slackWorkspaceLinkRepository.findByTeamId(teamId)

            if (existingLink != null) {
                val updatedLink = SlackWorkspaceLink(
                    id = existingLink.id,
                    teamId = teamId,
                    accessToken = accessToken,
                    organizationId = existingLink.organizationId
                )
                slackWorkspaceLinkRepository.save(updatedLink)
            } else {
                val newLink = SlackWorkspaceLink(
                    teamId = teamId,
                    accessToken = accessToken,
                    organizationId = null
                )
                slackWorkspaceLinkRepository.save(newLink)
            }

            try {
                slack.methods(accessToken).chatPostEphemeral {
                    it.channel(installerUserId)
                        .user(installerUserId)
                        .text(
                            """
                          환영합니다! 🎉 SPACER 예약 봇이 성공적으로 설치되었습니다.
                          이제 마지막 단계로, 아무 채널에서나 `/워크스페이스등록` 명령어를 입력하여
                          현재 워크스페이스를 당신의 단체와 연동해주세요.
                          """.trimIndent()
                        )
                }
            } catch (e: Exception) {
                logger.error("설치 후 환영 메시지 전송 실패: teamId=$teamId", e)
            }

            return "✅ 예약 봇이 워크스페이스에 성공적으로 설치되었습니다!"
        } else {
            logger.error("Slack API Error: {}", response.error)
            return "설치에 실패했습니다: ${response.error}"
        }
    }
}
