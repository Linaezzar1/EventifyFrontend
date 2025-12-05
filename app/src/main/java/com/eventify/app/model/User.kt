package com.eventify.app.model

// Model User complet correspondant au backend
data class User(
    val _id: String,
    val name: String,
    val email: String,
    val role: String, // "participant", "organisateur", "logistique", "communication"
    val avatarUrl: String? = null,
    val notificationPreferences: NotificationPreferences? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class NotificationPreferences(
    val email: Boolean = true,
    val push: Boolean = true
)

// Pour les mises à jour de profil
data class UpdateProfileRequest(
    val name: String? = null,
    val avatarUrl: String? = null,
    val notificationPreferences: NotificationPreferences? = null
)

