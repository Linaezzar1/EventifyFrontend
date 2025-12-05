package com.eventify.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventify.app.model.ChatbotRequest
import com.eventify.app.model.ChatbotResponse
import com.eventify.app.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: Int,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: String = ""
)

class ChatbotViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = 0,
                text = "Bonjour ! 👋 Je suis l'assistant Eventify. Comment puis-je vous aider aujourd'hui ?",
                isFromUser = false
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Envoyer un message au chatbot
    fun sendMessage(token: String, userMessage: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            // Ajouter le message de l'utilisateur
            val userMsg = ChatMessage(
                id = _messages.value.size,
                text = userMessage,
                isFromUser = true
            )
            _messages.value = _messages.value + userMsg

            _isLoading.value = true
            try {
                val request = ChatbotRequest(message = userMessage)
                val response = RetrofitInstance.api.chatWithBot("Bearer $token", request)

                // Ajouter la réponse du bot (utilise "response" du backend)
                val botMsg = ChatMessage(
                    id = _messages.value.size,
                    text = response.response,
                    isFromUser = false
                )
                _messages.value = _messages.value + botMsg
            } catch (e: Exception) {
                // En cas d'erreur, ajouter un message d'erreur
                val errorMsg = ChatMessage(
                    id = _messages.value.size,
                    text = "Désolé, je n'ai pas pu traiter votre demande. Veuillez réessayer. (${e.message})",
                    isFromUser = false
                )
                _messages.value = _messages.value + errorMsg
                onError(e.localizedMessage ?: "Erreur de communication avec le chatbot")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Réinitialiser la conversation
    fun clearConversation() {
        _messages.value = listOf(
            ChatMessage(
                id = 0,
                text = "Bonjour ! 👋 Je suis l'assistant Eventify. Comment puis-je vous aider aujourd'hui ?",
                isFromUser = false
            )
        )
    }
}

