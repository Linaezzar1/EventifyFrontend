package com.eventify.app.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class SignupRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String = "participant"
)

data class LoginResponse(
    val token: String,
    val role: String,
    val name: String,
    val email: String,
    val userId: String? = null, // ID de l'utilisateur
    val _id: String? = null // Alternative pour l'ID
) {
    // Helper pour obtenir l'ID quel que soit le champ retourné par le backend
    fun getId(): String = userId ?: _id ?: ""
}
