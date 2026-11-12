package com.aifactory.authservice.service

import com.aifactory.authservice.exception.RedisConnectionException
import org.slf4j.LoggerFactory
import org.springframework.data.redis.RedisConnectionFailureException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RedisService(
    private val redisTemplate: RedisTemplate<String, String>
) {

    private val logger = LoggerFactory.getLogger(RedisService::class.java)

    fun setValueWithExpiration(key: String, value: String, expiration: Long, timeUnit: TimeUnit) {
        try {
            redisTemplate.opsForValue().set(key, value, expiration, timeUnit)
        } catch (e: RedisConnectionFailureException) {
            logger.error("Redis connection failed while setting value: ${e.message}", e)
            throw RedisConnectionException("Redis 연결에 실패했습니다")
        } catch (e: Exception) {
            logger.error("Unexpected error while setting Redis value: ${e.message}", e)
            throw RedisConnectionException("Redis 저장 중 오류가 발생했습니다")
        }
    }

    fun getValue(key: String): String? {
        return try {
            redisTemplate.opsForValue().get(key)
        } catch (e: RedisConnectionFailureException) {
            logger.error("Redis connection failed while getting value: ${e.message}", e)
            throw RedisConnectionException("Redis 연결에 실패했습니다")
        } catch (e: Exception) {
            logger.error("Unexpected error while getting Redis value: ${e.message}", e)
            throw RedisConnectionException("Redis 조회 중 오류가 발생했습니다")
        }
    }

    fun deleteValue(key: String) {
        try {
            redisTemplate.delete(key)
        } catch (e: RedisConnectionFailureException) {
            logger.error("Redis connection failed while deleting value: ${e.message}", e)
            throw RedisConnectionException("Redis 연결에 실패했습니다")
        } catch (e: Exception) {
            logger.error("Unexpected error while deleting Redis value: ${e.message}", e)
            throw RedisConnectionException("Redis 삭제 중 오류가 발생했습니다")
        }
    }

    fun deleteAllByUsername(username: String) {
        try {
            val pattern = "refresh_token:$username:*"

            // 패턴에 매칭되는 모든 키 검색
            val keys = redisTemplate.keys(pattern)

            // 키가 존재하면 삭제
            if (keys?.isNotEmpty() == true) {
                redisTemplate.delete(keys)
            }
        } catch (e: RedisConnectionFailureException) {
            logger.error("Redis connection failed while deleting all values: ${e.message}", e)
            throw RedisConnectionException("Redis 연결에 실패했습니다")
        } catch (e: Exception) {
            logger.error("Unexpected error while deleting all Redis values: ${e.message}", e)
            throw RedisConnectionException("Redis 삭제 중 오류가 발생했습니다")
        }
    }

    fun hasKey(key: String): Boolean {
        return try {
            redisTemplate.hasKey(key)
        } catch (e: RedisConnectionFailureException) {
            logger.error("Redis connection failed while checking key: ${e.message}", e)
            throw RedisConnectionException("Redis 연결에 실패했습니다")
        } catch (e: Exception) {
            logger.error("Unexpected error while checking Redis key: ${e.message}", e)
            throw RedisConnectionException("Redis 조회 중 오류가 발생했습니다")
        }
    }
}