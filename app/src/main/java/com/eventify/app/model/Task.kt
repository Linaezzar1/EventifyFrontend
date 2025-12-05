package com.eventify.app.model

data class Task(
    val _id: String,
    val title: String,
    val description: String?,
    val status: String, // "en_attente", "en_cours", "termine"
    val assignedTo: UserRef?,
    val dueDate: String?,
    val event: String, // eventId
    val createdBy: UserRef?,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// Statuts possibles pour les tâches
object TaskStatus {
    const val EN_ATTENTE = "en_attente"
    const val EN_COURS = "en_cours"
    const val TERMINE = "termine"
}

