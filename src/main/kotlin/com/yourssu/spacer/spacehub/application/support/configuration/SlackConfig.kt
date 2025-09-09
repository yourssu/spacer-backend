package com.yourssu.spacer.spacehub.application.support.configuration

import com.slack.api.bolt.App
import com.slack.api.bolt.AppConfig
import com.slack.api.bolt.model.Bot
import com.slack.api.bolt.model.Installer
import com.slack.api.bolt.service.InstallationService
import com.yourssu.spacer.spacehub.application.domain.slack.BotImpl
import com.yourssu.spacer.spacehub.application.domain.slack.SlackCommandListener
import com.yourssu.spacer.spacehub.implement.domain.slack.SlackWorkspaceLinkReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SlackConfig(
    private val slackWorkspaceLinkReader: SlackWorkspaceLinkReader,
    private val slackCommandListener: SlackCommandListener,
    @Value("\${slack.signing-secret}") private val signingSecret: String
) {
    @Bean
    fun slackApp(): App {
        val appConfig = AppConfig.builder().signingSecret(signingSecret).build()
        val app = App(appConfig)

        app.service(object : InstallationService {
            override fun findBot(enterpriseId: String?, teamId: String): Bot? {
                val link = slackWorkspaceLinkReader.getByTeamId(teamId)
                return link.let { BotImpl(botAccessToken = it.accessToken, teamId = it.teamId) }
            }
            override fun saveInstallerAndBot(installer: Installer?) {}
            override fun deleteBot(bot: Bot?) {}
            override fun deleteInstaller(installer: Installer?) {}
            override fun findInstaller(enterpriseId: String?, teamId: String?, userId: String?): Installer? = null
            override fun isHistoricalDataEnabled(): Boolean = false
            override fun setHistoricalDataEnabled(isHistoricalDataEnabled: Boolean) {}
        })

        slackCommandListener.applyTo(app)

        return app
    }
}
