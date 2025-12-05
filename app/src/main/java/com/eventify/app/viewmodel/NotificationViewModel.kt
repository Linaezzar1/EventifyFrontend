package com.eventify.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventify.app.model.Notification
import com.eventify.app.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Charger les notifications
    fun loadNotifications(token: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val notifList = RetrofitInstance.api.getMyNotifications("Bearer $token")
                _notifications.value = notifList
                _unreadCount.value = notifList.count { !it.read }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Erreur de chargement des notifications")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Marquer une notification comme lue
    fun markAsRead(token: String, notificationId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.markNotificationAsRead("Bearer $token", notificationId)
                if (response.isSuccessful) {
                    // Mettre à jour localement
                    _notifications.value = _notifications.value.map { notif ->
                        if (notif._id == notificationId) notif.copy(read = true) else notif
                    }
                    _unreadCount.value = _notifications.value.count { !it.read }
                    onSuccess()
                } else {
                    onError("Erreur lors du marquage")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Erreur")
            }
        }
    }

    // Marquer toutes comme lues
    fun markAllAsRead(token: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            val unreadNotifs = _notifications.value.filter { !it.read }
            for (notif in unreadNotifs) {
                try {
                    RetrofitInstance.api.markNotificationAsRead("Bearer $token", notif._id)
                } catch (e: Exception) {
                    // Continuer même en cas d'erreur
                }
            }
            // Recharger les notifications
            loadNotifications(token, onError)
        }
    }
}

