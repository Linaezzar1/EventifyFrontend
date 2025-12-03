package com.eventify.app.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class SignupRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String
)

data class LoginResponse(
    val token: String,
    val role: String,
    val name: String,
    val email: String
)
