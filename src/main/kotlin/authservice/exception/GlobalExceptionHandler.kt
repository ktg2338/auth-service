package com.aifactory.authservice.exception

import com.aifactory.authservice.dto.response.ErrorResponse
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.security.SignatureException
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestCookieException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(InvalidTokenException::class)
    fun handleInvalidTokenException(ex: InvalidTokenException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.error("Invalid token: ${ex.message}")
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.UNAUTHORIZED.value(),
                error = "INVALID_TOKEN",
                message = ex.message ?: "유효하지 않은 토큰입니다",
                path = request.requestURI
            ))
    }

    @ExceptionHandler(TokenExpiredException::class)
    fun handleTokenExpiredException(ex: TokenExpiredException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.error("Token expired: ${ex.message}")
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.UNAUTHORIZED.value(),
                error = "TOKEN_EXPIRED",
                message = ex.message ?: "토큰이 만료되었습니다",
                path = request.requestURI
            ))
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialsException(ex: BadCredentialsException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.error("Bad credentials: ${ex.message}")
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.UNAUTHORIZED.value(),
                error = "INVALID_CREDENTIALS",
                message = "아이디 또는 비밀번호가 올바르지 않습니다",
                path = request.requestURI
            ))
    }

    @ExceptionHandler(UsernameNotFoundException::class)
    fun handleUsernameNotFoundException(ex: UsernameNotFoundException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.error("User not found: ${ex.message}")
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.NOT_FOUND.value(),
                error = "USER_NOT_FOUND",
                message = "사용자를 찾을 수 없습니다",
                path = request.requestURI
            ))
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(ex: UserNotFoundException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.error("User not found: ${ex.message}")
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.NOT_FOUND.value(),
                error = "USER_NOT_FOUND",
                message = ex.message ?: "사용자를 찾을 수 없습니다",
                path = request.requestURI
            ))
    }

    @ExceptionHandler(DuplicateUsernameException::class)
    fun handleDuplicateUsernameException(ex: DuplicateUsernameException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.error("Duplicate username: ${ex.message}")
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.CONFLICT.value(),
                error = "DUPLICATE_USERNAME",
                message = ex.message ?: "이미 사용중인 아이디입니다",
                path = request.requestURI
            ))
    }

    @ExceptionHandler(DuplicateEmailException::class)
    fun handleDuplicateEmailException(ex: DuplicateEmailException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.error("Duplicate email: ${ex.message}")
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.CONFLICT.value(),
                error = "DUPLICATE_EMAIL",
                message = ex.message ?: "이미 사용중인 이메일입니다",
                path = request.requestURI
            ))
    }

    @ExceptionHandler(RedisConnectionException::class)
    fun handleRedisConnectionException(ex: RedisConnectionException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.error("Redis connection error: ${ex.message}", ex)
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.SERVICE_UNAVAILABLE.value(),
                error = "SERVICE_UNAVAILABLE",
                message = "일시적인 서비스 오류가 발생했습니다. 잠시 후 다시 시도해주세요",
                path = request.requestURI
            ))
    }

    @ExceptionHandler(ExpiredJwtException::class)
    fun handleExpiredJwtException(ex: ExpiredJwtException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.error("JWT expired: ${ex.message}")
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.UNAUTHORIZED.value(),
                error = "TOKEN_EXPIRED",
                message = "토큰이 만료되었습니다",
                path = request.requestURI
            ))
    }

    @ExceptionHandler(MalformedJwtException::class, SignatureException::class)
    fun handleInvalidJwtException(ex: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.error("Invalid JWT: ${ex.message}")
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.UNAUTHORIZED.value(),
                error = "INVALID_TOKEN",
                message = "유효하지 않은 토큰입니다",
                path = request.requestURI
            ))
    }

    @ExceptionHandler(MissingRequestCookieException::class)
    fun handleMissingRequestCookieException(ex: MissingRequestCookieException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.error("Missing cookie: ${ex.message}")
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.BAD_REQUEST.value(),
                error = "MISSING_COOKIE",
                message = "${ex.cookieName} 쿠키가 필요합니다",
                path = request.requestURI
            ))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        logger.error("Validation failed: $errors")
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.BAD_REQUEST.value(),
                error = "VALIDATION_FAILED",
                message = errors.joinToString(", "),
                path = request.requestURI
            ))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error occurred", ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error = "INTERNAL_SERVER_ERROR",
                message = "서버 오류가 발생했습니다",
                path = request.requestURI
            ))
    }
}