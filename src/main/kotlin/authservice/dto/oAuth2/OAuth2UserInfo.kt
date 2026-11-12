package com.aifactory.authservice.dto.oAuth2

abstract class OAuth2UserInfo(
    protected val attributes: Map<String, Any>
) {
    abstract val id: String
    abstract val name: String
    abstract val email: String
    abstract val imageUrl: String?
}
