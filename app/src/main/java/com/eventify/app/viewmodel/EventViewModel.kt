package com.eventify.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventify.app.model.Event
import com.eventify.app.model.EventRequest
import com.eventify.app.model.Task
import com.eventify.app.model.TaskRequest
import com.eventify.app.model.User
import com.eventify.app.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EventViewModel : ViewModel() {
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private val _selectedEvent = MutableStateFlow<Event?>(null)
    val selectedEvent: StateFlow<Event?> = _selectedEvent

    fun loadEvents(token: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val eventsList = RetrofitInstance.api.getEvents("Bearer $token")
                _events.value = eventsList
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Erreur de chargement")
            }
        }
    }

    fun loadEventById(token: String, id: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val event = RetrofitInstance.api.getEventById("Bearer $token", id)
                _selectedEvent.value = event
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Erreur de chargement")
            }
        }
    }

    fun createEvent(token: String, eventRequest: EventRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.createEvent("Bearer $token", eventRequest)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Erreur de création")
            }
        }
    }

    fun updateEvent(token: String, eventId: String, eventRequest: EventRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.updateEvent("Bearer $token", eventId, eventRequest)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Erreur de modification")
            }
        }
    }

    fun deleteEvent(token: String, eventId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.deleteEvent("Bearer $token", eventId)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Erreur à la suppression")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Erreur inconnue")
            }
        }
    }

    fun joinEvent(token: String, eventId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.joinEvent("Bearer $token", eventId)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Erreur lors de l'inscription")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Erreur inscription")
            }
        }
    }

    fun leaveEvent(token: String, eventId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.leaveEvent("Bearer $token", eventId)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Erreur lors de la désinscription")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Erreur désinscription")
            }
        }
    }

    fun loadParticipants(token: String, eventId: String, onSuccess: (List<User>) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val participants = RetrofitInstance.api.getParticipants("Bearer $token", eventId)
                onSuccess(participants)
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Erreur chargement participants")
            }
        }
    }

    fun createTask(token: String, eventId: String, taskRequest: TaskRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.createTask("Bearer $token", eventId, taskRequest)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Erreur création tâche")
            }
        }
    }

    fun getTasksForEvent(token: String, eventId: String, onSuccess: (List<Task>) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val tasks = RetrofitInstance.api.getTasksForEvent("Bearer $token", eventId)
                onSuccess(tasks)
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Erreur chargement tâches")
            }
        }
    }

    fun updateTask(token: String, taskId: String, taskRequest: TaskRequest, onSuccess: (Task) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val updatedTask = RetrofitInstance.api.updateTask("Bearer $token", taskId, taskRequest)
                onSuccess(updatedTask)
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Erreur mise à jour tâche")
            }
        }
    }

    fun deleteTask(token: String, taskId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.deleteTask("Bearer $token", taskId)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Erreur suppression tâche")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Erreur suppression tâche")
            }
        }
    }

}
