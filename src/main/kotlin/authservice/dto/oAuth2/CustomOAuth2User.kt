package com.aifactory.authservice.dto.oAuth2

import com.aifactory.authservice.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User

class CustomOAuth2User(
    private val user: User,
    private val attributes: Map<String, Any>
) : OAuth2User {

    companion object {
        fun create(user: User, attributes: Map<String, Any>): CustomOAuth2User {
            return CustomOAuth2User(user, attributes)
        }
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_${user.roles}"))
    }

    override fun getAttributes(): Map<String, Any> = attributes

    override fun getName(): String = user.email

    fun getUser(): User = user
}