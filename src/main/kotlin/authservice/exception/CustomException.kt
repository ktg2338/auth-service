package com.aifactory.authservice.exception

class InvalidTokenException(message: String) : RuntimeException(message)

class TokenExpiredException(message: String) : RuntimeException(message)

class DuplicateUsernameException(message: String) : RuntimeException(message)

class DuplicateEmailException(message: String) : RuntimeException(message)

class UserNotFoundException(message: String) : RuntimeException(message)

class RedisConnectionException(message: String) : RuntimeException(message)