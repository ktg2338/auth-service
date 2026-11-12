package com.aifactory.authservice.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.CascadeType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "users")
data class User (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long =0L,

    @Column(unique = true, nullable = false)
    val username: String,

    @Column(nullable = false)
    val password: String,

    val name: String,
    val email: String,

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonBackReference
    val oAuth2Subjects: MutableList<OAuth2Subject> = mutableListOf(),

    @ElementCollection(targetClass = UserRole::class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")]
    )
    var roles: MutableSet<UserRole> = mutableSetOf()
)