package com.eventify.app.network

import com.eventify.app.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @POST("api/auth/signup")
    suspend fun signup(@Body signupRequest: SignupRequest): Response<Unit>

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

    @PUT("api/events/tasks/{taskId}")
    suspend fun updateTask(
        @Header("Authorization") token: String,
        @Path("taskId") taskId: String,
        @Body taskRequest: TaskRequest
    ): Task

    @DELETE("api/events/tasks/{taskId}")
    suspend fun deleteTask(
        @Header("Authorization") token: String,
        @Path("taskId") taskId: String
    ): Response<Unit>

    // User endpoints (matching backend routes)
    @DELETE("api/users/{id}")
    suspend fun deleteUser(
        @Header("Authorization") token: String,
        @Path("id") userId: String
    ): Response<Unit>

}
