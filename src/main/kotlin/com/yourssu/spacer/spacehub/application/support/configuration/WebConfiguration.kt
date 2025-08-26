package com.yourssu.spacer.spacehub.application.support.configuration

import com.yourssu.spacer.spacehub.application.support.authentication.AuthenticationInterceptor
import com.yourssu.spacer.spacehub.application.support.authentication.AuthenticationOrganizationInfoArgumentResolver
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableConfigurationProperties(CorsProperties::class)
class WebConfiguration(
    private val authenticationInterceptor: AuthenticationInterceptor,
    private val authenticationOrganizationInfoArgumentResolver: AuthenticationOrganizationInfoArgumentResolver,
    private val corsProperties: CorsProperties,
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authenticationInterceptor)
            .addPathPatterns("/**")
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(authenticationOrganizationInfoArgumentResolver)
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins(*corsProperties.allowedOrigins)
            .allowedHeaders("*")
            .allowedMethods(*HttpMethod.values().map { it.name() }.toTypedArray())
            .exposedHeaders(HttpHeaders.LOCATION)
            .allowCredentials(true)
    }
}
