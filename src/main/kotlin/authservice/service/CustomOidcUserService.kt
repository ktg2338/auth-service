package com.aifactory.authservice.service

import com.aifactory.authservice.security.OAuth2UserInfoFactory
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service

@Service
class CustomOidcUserService(
) : OidcUserService() {

    private val logger = LoggerFactory.getLogger(CustomOidcUserService::class.java)

    override fun loadUser(userRequest: OidcUserRequest): OidcUser {

        val oidcUser = try {
            super.loadUser(userRequest)
        } catch (e: Exception) {
            logger.error("Failed to load OIDC user: ${e.message}", e)
            throw OAuth2AuthenticationException("invalid_user_info_response")
        }
        val registrationId = userRequest.clientRegistration.registrationId
        val userInfo = try {
            OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oidcUser.attributes)
        } catch (e: Exception) {
            logger.error("Failed to extract OAuth2 user info from provider $registrationId: ${e.message}", e)
            throw OAuth2AuthenticationException("invalid_user_info_response")
        }
        if (userInfo.email.isBlank()) {
            logger.warn("Email is empty for user from provider $registrationId")
            throw OAuth2AuthenticationException("email_is_empty")
        }
        return oidcUser
    }

}