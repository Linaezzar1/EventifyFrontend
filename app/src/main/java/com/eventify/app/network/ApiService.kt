package com.eventify.app.network

import com.eventify.app.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ==================== AUTH ====================
    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @POST("api/auth/signup")
    suspend fun signup(@Body signupRequest: SignupRequest): Response<Unit>

    // ==================== USERS ====================
    @GET("api/users")
    suspend fun getAllUsers(@Header("Authorization") token: String): List<User>

    @DELETE("api/users/{id}")
    suspend fun deleteUser(
        @Header("Authorization") token: String,
        @Path("id") userId: String
    ): Response<Unit>

    // ==================== EVENTS ====================
    @GET("api/events")
    suspend fun getEvents(@Header("Authorization") token: String): List<Event>

    @GET("api/events/{id}")
    suspend fun getEventById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Event

    @POST("api/events")
    suspend fun createEvent(
        @Header("Authorization") token: String,
        @Body eventRequest: EventRequest
    ): Event

    @PUT("api/events/{id}")
    suspend fun updateEvent(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body eventRequest: EventRequest
    ): Event

    @DELETE("api/events/{id}")
    suspend fun deleteEvent(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>

    // Inscriptions participant
    @POST("api/events/{id}/join")
    suspend fun joinEvent(
        @Header("Authorization") token: String,
        @Path("id") eventId: String
    ): Response<Unit>

    @HTTP(method = "DELETE", path = "api/events/{id}/join", hasBody = false)
    suspend fun leaveEvent(
        @Header("Authorization") token: String,
        @Path("id") eventId: String
    ): Response<Unit>

    @GET("api/events/{id}/participants")
    suspend fun getParticipants(
        @Header("Authorization") token: String,
        @Path("id") eventId: String
    ): List<User>

    // ==================== TASKS ====================
    @POST("api/events/{eventId}/tasks")
    suspend fun createTask(
        @Header("Authorization") token: String,
        @Path("eventId") eventId: String,
        @Body taskRequest: TaskRequest
    ): Task

    @GET("api/events/{eventId}/tasks")
    suspend fun getTasksForEvent(
        @Header("Authorization") token: String,
        @Path("eventId") eventId: String
    ): List<Task>

    @PUT("api/events/{eventId}/tasks/{taskId}")
    suspend fun updateTask(
        @Header("Authorization") token: String,
        @Path("eventId") eventId: String,
        @Path("taskId") taskId: String,
        @Body taskRequest: TaskRequest
    ): Task

    @DELETE("api/events/{eventId}/tasks/{taskId}")
    suspend fun deleteTask(
        @Header("Authorization") token: String,
        @Path("eventId") eventId: String,
        @Path("taskId") taskId: String
    ): Response<Unit>

    // ==================== MESSAGES ====================
    @POST("api/messages")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Body messageRequest: SendMessageRequest
    ): Message

    @GET("api/messages")
    suspend fun getInbox(@Header("Authorization") token: String): List<Message>

    @GET("api/messages/{otherUserId}")
    suspend fun getConversation(
        @Header("Authorization") token: String,
        @Path("otherUserId") otherUserId: String
    ): List<Message>

    // ==================== NOTIFICATIONS ====================
    @GET("api/notifications")
    suspend fun getMyNotifications(@Header("Authorization") token: String): List<Notification>

    @PUT("api/notifications/{id}/read")
    suspend fun markNotificationAsRead(
        @Header("Authorization") token: String,
        @Path("id") notificationId: String
    ): Response<Unit>

    // ==================== CHATBOT ====================
    @POST("api/chatbot/chat")
    suspend fun chatWithBot(
        @Header("Authorization") token: String,
        @Body chatRequest: ChatbotRequest
    ): ChatbotResponse
}
