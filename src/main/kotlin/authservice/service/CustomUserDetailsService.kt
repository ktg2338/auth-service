package com.aifactory.authservice.service


import com.aifactory.authservice.repository.UserRepository
import com.aifactory.authservice.security.CustomUserDetails
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {

    private val logger = LoggerFactory.getLogger(CustomUserDetailsService::class.java)

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        return try {
            val user = userRepository.findByUsername(username)
                ?: throw UsernameNotFoundException("사용자를 찾을 수 없습니다: $username")
            CustomUserDetails(user)
        } catch (e: UsernameNotFoundException) {
            logger.error("User not found: $username")
            throw e
        } catch (e: DataAccessException) {
            logger.error("Database error while loading user: ${e.message}", e)
            throw RuntimeException("데이터베이스 오류가 발생했습니다")
        } catch (e: Exception) {
            logger.error("Unexpected error while loading user: ${e.message}", e)
            throw RuntimeException("사용자 정보 조회 중 오류가 발생했습니다")
        }
    }
}