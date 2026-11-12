package com.aifactory.authservice.config

import com.aifactory.authservice.handler.OAuth2FailureHandler
import com.aifactory.authservice.handler.OAuth2SuccessHandler
import com.aifactory.authservice.security.JwtAuthenticationFilter
import com.aifactory.authservice.service.CustomOAuth2UserService
import com.aifactory.authservice.service.CustomOidcUserService
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties::class)
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val oAuth2SuccessHandler: OAuth2SuccessHandler,
    private val oAuth2FailureHandler: OAuth2FailureHandler,
    private val customOidcUserService: CustomOidcUserService
) {
    private val logger = LoggerFactory.getLogger(SecurityConfig::class.java)

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager =
        authConfig.authenticationManager

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOriginPatterns = listOf(
            "http://localhost:*",
            "https://auth-prod.purpleriver-9377ba31.koreacentral.azurecontainerapps.io",
            "https://gateway-prod.purpleriver-9377ba31.koreacentral.azurecontainerapps.io"
        )
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/auth/login", "/auth/signup",
                        "/oauth2/**", "/auth/login/success",
                        "/actuator/health",
                        "/swagger-ui/**", "/swagger-ui.html",
                        "/api-docs/**", "/v3/api-docs/**",
                        "/webjars/**"
                    )
                    .permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2Login { oauth2 ->
                oauth2
                    .authorizationEndpoint { auth ->
                        auth.baseUri("/oauth2/authorize")
                    }
                    .redirectionEndpoint { redirect ->
                        redirect.baseUri("/login/oauth2/code/*")
                    }
                    .userInfoEndpoint { endpoint ->
                        endpoint.userService(customOAuth2UserService) //기본 OAuth2 (kakao 권장)
                        endpoint.oidcUserService(customOidcUserService)  //OIDC용 (google 권장)
                    }
                    .successHandler(oAuth2SuccessHandler)
                    .failureHandler(oAuth2FailureHandler)
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling {
                it.authenticationEntryPoint { _, response, _ ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                }
            }
            .build()
    }
}