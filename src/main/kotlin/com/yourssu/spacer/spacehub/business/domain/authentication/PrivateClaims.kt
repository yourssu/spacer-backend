package com.yourssu.spacer.spacehub.business.domain.authentication

import io.jsonwebtoken.Claims

data class PrivateClaims(
    val organizationId: Long,
) {

    companion object {
        private const val ORGANIZATION_ID_KEY_NAME = "organizationId"

        fun from(claims: Claims): PrivateClaims {
            return PrivateClaims((claims[ORGANIZATION_ID_KEY_NAME] as Number).toLong())
        }
    }

    fun toMap(): Map<String, Any> {
        return mapOf(ORGANIZATION_ID_KEY_NAME to organizationId)
    }
}
