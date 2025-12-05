package com.eventify.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventify.app.model.AdminStats
import com.eventify.app.model.Event
import com.eventify.app.model.RoleUpdateRequest
import com.eventify.app.model.User
import com.eventify.app.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.collections.forEach
import kotlin.collections.set
import kotlin.collections.toList

class AdminViewModel : ViewModel() {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    // Collect unique users from all events' participants
    fun collectUsersFromEvents(events: List<Event>, token: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val allParticipants = mutableSetOf<String>()
                val userMap = mutableMapOf<String, User>()
                
                // Collect all participant IDs from events
                events.forEach { event ->
                    event.participants?.forEach { participantId ->
                        allParticipants.add(participantId)
                    }
                }
                
                // Fetch participant details for each event
                events.forEach { event ->
                    try {
                        val participants = RetrofitInstance.api.getParticipants("Bearer $token", event._id)
                        participants.forEach { user ->
                            userMap[user._id] = user
                        }
                    } catch (e: Exception) {
                        // Continue if one fails
                    }
                }
                
                _users.value = userMap.values.toList()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Erreur de chargement des utilisateurs")
            } finally {
                _loading.value = false
            }
        }
    }


    fun deleteUser(token: String, userId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.deleteUser("Bearer $token", userId)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Erreur lors de la suppression")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Erreur lors de la suppression")
            }
        }
    }
}

