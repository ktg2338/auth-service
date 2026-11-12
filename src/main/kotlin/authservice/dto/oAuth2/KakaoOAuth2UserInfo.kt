package com.aifactory.authservice.dto.oAuth2


class KakaoOAuth2UserInfo(
    attributes: Map<String, Any>
) : OAuth2UserInfo(attributes) {
    override val id: String
        get() = attributes["id"].toString()

    override val name: String
        get() {
            val properties = attributes["properties"] as? Map<String, Any>
            return properties?.get("nickname")?.toString() ?: ""
        }

    override val email: String
        get() {
            val kakaoAccount = attributes["kakao_account"] as? Map<String, Any>
            return kakaoAccount?.get("email")?.toString() ?: ""
        }

    override val imageUrl: String?
        get() {
            val properties = attributes["properties"] as? Map<String, Any>
            return properties?.get("profile_image")?.toString()
        }
}