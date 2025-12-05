package com.eventify.app.model

// Request et Response pour le Chatbot
data class ChatbotRequest(
    val message: String,
    val conversationHistory: List<ConversationMessage>? = null
)

data class ConversationMessage(
    val role: String, // "user" ou "assistant"
    val content: String
)

data class ChatbotResponse(
    val response: String, // Le backend renvoie "response" pas "reply"
    val conversationId: Long? = null,
    val source: String? = null, // "ai-groq" ou "local"
    val userContext: UserContext? = null
)

data class UserContext(
    val role: String,
    val eventsCount: Int,
    val pendingTasksCount: Int
)

