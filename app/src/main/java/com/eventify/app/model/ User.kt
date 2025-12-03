package com.eventify.app.model

data class User(
    val _id: String,
    val name: String,
    val email: String,
    val role: String // "participant", "organisateur", "logistique", "communication"
)
