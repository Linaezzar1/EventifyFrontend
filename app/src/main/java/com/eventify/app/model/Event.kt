package com.eventify.app.model

data class Event(
    val _id: String,
    val title: String,
    val description: String?,
    val date: String,
    val location: String?,
    val createdBy: UserRef?,
    val organizers: List<String>? = emptyList(),
    val logisticManager: String? = null,
    val communicationManager: String? = null,
    val participants: List<String>? = emptyList(),
    val logisticStaff: List<String>? = emptyList(),
    val communicationStaff: List<String>? = emptyList(),
    val visibility: String? = "public", // "public" ou "prive"
    val status: String? = "brouillon", // "brouillon", "publie", "annule"
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// Référence utilisateur simplifiée (pour createdBy, etc.)
data class UserRef(
    val _id: String,
    val name: String,
    val email: String,
    val role: String
)

data class EventRequest(
    val title: String,
    val description: String?,
    val date: String,
    val location: String?,
    val visibility: String? = "public",
    val status: String? = "brouillon",
    val logisticManager: String? = null
)
