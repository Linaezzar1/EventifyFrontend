package com.eventify.app.model

data class TaskRequest(
    val title: String,
    val description: String?,
    val status: String,
    val assignedTo: String?,
    val dueDate: String?
)