package com.yourssu.spacer

import com.yourssu.spacer.spacehub.application.domain.discord.DiscordBot
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class SpacerApplicationTests {

    @MockBean
    private lateinit var discordBot: DiscordBot

    @Test
    fun contextLoads() {
    }
}
