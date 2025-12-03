package com.eventify.app.model

data class Task(
    val _id: String,
    val title: String,
    val description: String?,
    val status: String,
    val assignedTo: User?,
    val dueDate: String?,
    val event: String,
    val createdBy: User?
)
