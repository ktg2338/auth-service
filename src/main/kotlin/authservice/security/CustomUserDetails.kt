package com.aifactory.authservice.security

import com.aifactory.authservice.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails (
    private val user : User
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority?> {
        return user.roles.map { role ->
            SimpleGrantedAuthority("ROLE_${role.name}")
        }
    }

    override fun getPassword(): String? {
        return user.password
    }

    override fun getUsername(): String? {
        return user.username
    }
}