package com.eventify.app.model

data class AdminStats(
    val totalUsers: Int,
    val totalEvents: Int,
    val totalParticipants: Int,
    val usersByRole: Map<String, Int>? = null
)

data class RoleUpdateRequest(
    val role: String
)

