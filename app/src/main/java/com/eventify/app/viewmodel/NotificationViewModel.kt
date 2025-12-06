import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventify.app.model.Notification
import com.eventify.app.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.collections.map

class NotificationViewModel : ViewModel() {
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    val isLoading = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)

    fun loadNotifications(token: String) {
        viewModelScope.launch {
            try {
                isLoading.value = true
                val auth = "Bearer $token"
                val result = RetrofitInstance.api.getMyNotifications(auth)
                _notifications.value = result
                error.value = null
            } catch (e: Exception) {
                error.value = e.message ?: "Erreur de chargement"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun markAsRead(token: String, notificationId: String) {
        viewModelScope.launch {
            try {
                val auth = "Bearer $token"
                RetrofitInstance.api.markNotificationAsRead(auth, notificationId)
                _notifications.value = _notifications.value.map {
                    if (it._id == notificationId) it.copy(read = true) else it
                }
            } catch (_: Exception) { }
        }
    }
}
