package com.eventify.app.model

data class Event(
    val _id: String,
    val title: String,
    val description: String?,
    val date: String,
    val location: String?,
    val createdBy: CreatedBy?,
    val participants: List<String>? = emptyList()
)

data class CreatedBy(
    val _id: String,
    val name: String,
    val email: String,
    val role: String
)

data class EventRequest(
    val title: String,
    val description: String,
    val date: String,
    val location: String
)
