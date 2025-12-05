package com.eventify.app.model

// Model Message correspondant au backend
data class Message(
    val _id: String,
    val sender: UserRef,
    val receiver: UserRef,
    val content: String,
    val read: Boolean = false,
    val createdAt: String
)

// Pour envoyer un message
data class SendMessageRequest(
    val receiverId: String,
    val content: String
)

// Conversation (pour l'affichage dans la liste)
data class Conversation(
    val otherUser: UserRef,
    val lastMessage: Message?,
    val unreadCount: Int = 0
)

