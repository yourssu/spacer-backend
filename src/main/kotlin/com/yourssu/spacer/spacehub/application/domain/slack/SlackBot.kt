package com.yourssu.spacer.spacehub.application.domain.slack

import com.slack.api.bolt.App
import com.slack.api.bolt.socket_mode.SocketModeApp
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SlackBot(
    private val app: App,
    @Value("\${slack.app-token}") private val appToken: String
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private var socketModeApp: SocketModeApp? = null

    @PostConstruct
    fun start() {
        socketModeApp = SocketModeApp(appToken, app)
        Thread {
            try { socketModeApp?.start() }
            catch (e: Exception) { logger.error("소켓 모드 앱 시작 중 에러 발생", e) }
        }.start()
        logger.info("Slack Socket Mode 앱 리스너를 시작합니다.")
    }

    @PreDestroy
    fun stop() {
        try { socketModeApp?.stop() }
        catch (e: Exception) { logger.error("소켓 모드 앱 중지 중 에러 발생", e) }
        logger.info("Slack Socket Mode 앱 리스너를 중지합니다.")
    }
}
