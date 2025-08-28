package com.yourssu.spacer.spacehub.implement.support.configuration

import com.yourssu.spacer.spacehub.implement.support.security.token.JwtProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(JwtProperties::class, DiscordProperties::class)
class PropertiesConfiguration
