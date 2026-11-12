package com.aifactory.authservice.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {

    @Value("\${spring.data.redis.host}")
    private lateinit var redisHost: String

    @Value("\${spring.data.redis.port}")
    private var redisPort: Int = 6379

    @Value("\${spring.data.redis.password}")
    private lateinit var redisPassword: String

    @Value("\${spring.data.redis.ssl.enabled:false}")
    private var sslEnabled: Boolean = false

    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        val redisStandaloneConfiguration = RedisStandaloneConfiguration().apply {
            hostName = redisHost
            port = redisPort
            setPassword(redisPassword)
        }

        //로컬에선 ssl 미적용
        val clientConfigurationBuilder = LettuceClientConfiguration.builder()
        if (sslEnabled) {
            clientConfigurationBuilder.useSsl()
        }
        val clientConfiguration = clientConfigurationBuilder.build()

        return LettuceConnectionFactory(redisStandaloneConfiguration, clientConfiguration)
    }

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        return RedisTemplate<String, Any>().apply {
            this.connectionFactory = connectionFactory
            keySerializer = StringRedisSerializer()
            valueSerializer = GenericJackson2JsonRedisSerializer()
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = GenericJackson2JsonRedisSerializer()
            afterPropertiesSet()
        }
    }
}