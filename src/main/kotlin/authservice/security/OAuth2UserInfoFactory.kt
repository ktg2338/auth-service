package com.aifactory.authservice.security


import com.aifactory.authservice.dto.oAuth2.GoogleOAuth2UserInfo
import com.aifactory.authservice.dto.oAuth2.KakaoOAuth2UserInfo
import com.aifactory.authservice.dto.oAuth2.OAuth2UserInfo
import org.springframework.security.oauth2.core.OAuth2AuthenticationException

object OAuth2UserInfoFactory {
    fun getOAuth2UserInfo(registrationId: String, attributes: Map<String, Any>): OAuth2UserInfo {
        return when (registrationId.lowercase()) {
            "google" -> GoogleOAuth2UserInfo(attributes)
            "kakao" -> KakaoOAuth2UserInfo(attributes)
            else -> throw OAuth2AuthenticationException("unsupported_oauth2_provider")
        }
    }
}
