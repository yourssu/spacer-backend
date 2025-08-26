package com.yourssu.spacer.spacehub.application.support.authentication

import com.yourssu.spacer.spacehub.business.domain.authentication.AuthenticationService
import com.yourssu.spacer.spacehub.business.support.security.token.TokenType
import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders
import org.springframework.lang.NonNull
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class AuthenticationOrganizationInfoArgumentResolver(
    private val authenticationService: AuthenticationService,
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(AuthenticationOrganization::class.java) &&
                parameter.parameterType == AuthenticationOrganizationInfo::class.java
    }

    override fun resolveArgument(
        @NonNull parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val accessToken: String? = webRequest.getHeader(HttpHeaders.AUTHORIZATION)
        if (accessToken.isNullOrBlank()) {
            if (isRequired(parameter)) {
                throw LoginRequiredException("로그인이 필요한 기능입니다.")
            }

            return null
        }

        val organizationId = authenticationService.getValidOrganizationId(TokenType.ACCESS, accessToken)

        return AuthenticationOrganizationInfo(organizationId)
    }

    private fun isRequired(parameter: MethodParameter): Boolean {
        return parameter.getParameterAnnotation(AuthenticationOrganization::class.java)
            ?.required ?: true
    }
}
