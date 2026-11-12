package com.aifactory.authservice.repository

import com.aifactory.authservice.entity.OAuth2Subject
import org.springframework.data.jpa.repository.JpaRepository

interface OAuth2SubjectRepository : JpaRepository<OAuth2Subject, String> {
    fun findBySubject(subject: String): OAuth2Subject?
}