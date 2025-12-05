package com.eventify.app.model

// Model Notification correspondant au backend
data class Notification(
    val _id: String,
    val receiver: String, // userId
    val sender: String? = null, // userId ou null si système
    val type: String, // "rappel_tache", "validation_inscription", "changement_horaire", "alerte_retard"
    val event: String? = null, // eventId
    val task: String? = null, // taskId
    val message: String? = null,
    val read: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// Types de notifications
object NotificationType {
    const val RAPPEL_TACHE = "rappel_tache"
    const val VALIDATION_INSCRIPTION = "validation_inscription"
    const val CHANGEMENT_HORAIRE = "changement_horaire"
    const val ALERTE_RETARD = "alerte_retard"
}

