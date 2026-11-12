package com.aifactory.authservice.service

import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service


@Service
class CustomOAuth2UserService(
) : DefaultOAuth2UserService() {

    private val logger = LoggerFactory.getLogger(CustomOAuth2UserService::class.java)

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = try {
            super.loadUser(userRequest)
        } catch (e: Exception) {
            logger.error("Failed to load OAuth2 user: ${e.message}", e)
            throw OAuth2AuthenticationException("invalid_user_info_response")
        }
        val registrationId = userRequest.clientRegistration.registrationId
        logger.info("OAuth2 user loaded successfully from provider: $registrationId")
//        val userInfo = try {
//            OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.attributes)
//        } catch (e: Exception) {
//            logger.error("Failed to extract OAuth2 user info from provider $registrationId: ${e.message}", e)
//            throw OAuth2AuthenticationException("invalid_user_info_response")
//        }

//        if (userInfo.email.isBlank()) {
//            logger.warn("Email is empty for user from provider $registrationId")
//            throw OAuth2AuthenticationException("email_is_empty")
//        }

        return oAuth2User
    }

}