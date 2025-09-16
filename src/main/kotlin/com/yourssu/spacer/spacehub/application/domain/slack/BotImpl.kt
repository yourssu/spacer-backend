package com.yourssu.spacer.spacehub.application.domain.slack

import com.slack.api.bolt.model.Bot

class BotImpl(
    private var botAccessToken: String,
    private var teamId: String
) : Bot {
    private var appId: String? = null
    private var isEnterpriseInstall: Boolean? = null
    private var enterpriseUrl: String? = null
    private var tokenType: String? = null
    private var enterpriseId: String? = null
    private var botId: String? = null
    private var botUserId: String? = null
    private var botScope: String? = null
    private var botRefreshToken: String? = null
    private var botTokenExpiresAt: Long? = null
    private var installedAt: Long? = null

    override fun getAppId(): String? = this.appId
    override fun setAppId(appId: String?) { this.appId = appId }

    override fun getIsEnterpriseInstall(): Boolean? = this.isEnterpriseInstall
    override fun setIsEnterpriseInstall(isEnterpriseInstall: Boolean?) { this.isEnterpriseInstall = isEnterpriseInstall }

    override fun getEnterpriseUrl(): String? = this.enterpriseUrl
    override fun setEnterpriseUrl(enterpriseUrl: String?) { this.enterpriseUrl = enterpriseUrl }

    override fun getTokenType(): String? = this.tokenType
    override fun setTokenType(tokenType: String?) { this.tokenType = tokenType }

    override fun getEnterpriseId(): String? = this.enterpriseId
    override fun setEnterpriseId(enterpriseId: String?) { this.enterpriseId = enterpriseId }

    override fun getTeamId(): String = this.teamId
    override fun setTeamId(teamId: String) { this.teamId = teamId }

    override fun getBotId(): String? = this.botId
    override fun setBotId(botId: String?) { this.botId = botId }

    override fun getBotUserId(): String? = this.botUserId
    override fun setBotUserId(botUserId: String?) { this.botUserId = botUserId }

    override fun getBotScope(): String? = this.botScope
    override fun setBotScope(scope: String?) { this.botScope = scope }

    override fun getBotAccessToken(): String = this.botAccessToken
    override fun setBotAccessToken(botAccessToken: String) { this.botAccessToken = botAccessToken }

    override fun getBotRefreshToken(): String? = this.botRefreshToken
    override fun setBotRefreshToken(botRefreshToken: String?) { this.botRefreshToken = botRefreshToken }

    override fun getBotTokenExpiresAt(): Long? = this.botTokenExpiresAt
    override fun setBotTokenExpiresAt(botTokenExpiresAt: Long?) { this.botTokenExpiresAt = botTokenExpiresAt }

    override fun getInstalledAt(): Long? = this.installedAt
    override fun setInstalledAt(installedAt: Long?) { this.installedAt = installedAt }
}
