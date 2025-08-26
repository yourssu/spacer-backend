package com.yourssu.spacer.spacehub.storage.domain.authentication

import com.yourssu.spacer.spacehub.business.domain.authentication.BlacklistToken
import com.yourssu.spacer.spacehub.business.support.security.token.TokenType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "blacklist_token")
class BlacklistTokenEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val organizationId: Long,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val tokenType: TokenType,

    @Column(nullable = false)
    val token: String,
) {

    companion object {
        fun from(blacklistToken: BlacklistToken) = BlacklistTokenEntity(
            id = blacklistToken.id,
            organizationId = blacklistToken.organizationId,
            tokenType = blacklistToken.tokenType,
            token = blacklistToken.token,
        )
    }

    fun toDomain() = BlacklistToken(
        id = id,
        organizationId = organizationId,
        tokenType = tokenType,
        token = token,
    )
}
