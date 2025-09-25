package com.yourssu.spacer.spacehub.application.domain.slack

import com.slack.api.model.block.Blocks
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.BlockCompositions
import com.slack.api.model.block.element.BlockElements
import com.yourssu.spacer.spacehub.business.domain.space.SpaceService
import com.yourssu.spacer.spacehub.implement.domain.slack.SlackWorkspaceLinkRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter

@Component
class SlackUIFactory(
    private val spaceService: SpaceService,
    private val slackWorkspaceLinkRepository: SlackWorkspaceLinkRepository
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun getVerifiedOrganizationId(teamId: String): Long? {
        return try {
            slackWorkspaceLinkRepository.findByTeamId(teamId)?.organizationId
        } catch (e: Exception) {
            logger.error("조직 정보 확인 중 오류 발생: teamId=$teamId", e)
            null
        }
    }

    fun createSpaceSelectMenu(actionId: String, text:String, placeholder: String, organizationId: Long): LayoutBlock {
        val spacesResult = spaceService.readAllByOrganizationId(organizationId)
        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        val options = spacesResult.spaceDtos.map { space ->
            val timeRange = "(${space.openingTime.format(formatter)}~${space.closingTime.format(formatter)})"
            BlockCompositions.option(
                BlockCompositions.plainText("${space.name} $timeRange"),
                space.id.toString()
            )
        }

        return Blocks.section { section ->
            section.text(BlockCompositions.markdownText(text))
            section.accessory(
                BlockElements.staticSelect { select ->
                    select.actionId(actionId)
                    select.placeholder(BlockCompositions.plainText(placeholder))
                    select.options(options)
                }
            )
        }
    }
}
