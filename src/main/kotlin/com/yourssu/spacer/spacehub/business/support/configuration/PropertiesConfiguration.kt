package com.yourssu.spacer.spacehub.business.support.configuration

import com.yourssu.spacer.spacehub.business.support.security.token.JwtProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class PropertiesConfiguration
