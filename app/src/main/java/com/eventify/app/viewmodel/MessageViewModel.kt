package com.eventify.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventify.app.model.Message
import com.eventify.app.model.SendMessageRequest
import com.eventify.app.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MessageViewModel : ViewModel() {

    private val _inbox = MutableStateFlow<List<Message>>(emptyList())
    val inbox: StateFlow<List<Message>> = _inbox

    private val _conversation = MutableStateFlow<List<Message>>(emptyList())
    val conversation: StateFlow<List<Message>> = _conversation

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Charger la boîte de réception (tous les messages)
    fun loadInbox(token: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val messages = RetrofitInstance.api.getInbox("Bearer $token")
                _inbox.value = messages
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Erreur de chargement des messages")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Charger une conversation avec un utilisateur spécifique
    fun loadConversation(token: String, otherUserId: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val messages = RetrofitInstance.api.getConversation("Bearer $token", otherUserId)
                _conversation.value = messages
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Erreur de chargement de la conversation")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Envoyer un message
    fun sendMessage(
        token: String,
        receiverId: String,
        content: String,
        onSuccess: (Message) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val request = SendMessageRequest(receiverId = receiverId, content = content)
                val message = RetrofitInstance.api.sendMessage("Bearer $token", request)
                // Ajouter le message à la conversation actuelle
                _conversation.value = _conversation.value + message
                onSuccess(message)
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Erreur d'envoi du message")
            }
        }
    }

    // Réinitialiser la conversation
    fun clearConversation() {
        _conversation.value = emptyList()
    }
}

