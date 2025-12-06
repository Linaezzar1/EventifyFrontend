package com.eventify.app.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventify.app.model.Task
import com.eventify.app.model.TaskRequest
import com.eventify.app.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel : ViewModel() {

    // Map<eventId, List<Task>>
    private val _tasksByEvent = MutableStateFlow<Map<String, List<Task>>>(emptyMap())
    val tasksByEvent: StateFlow<Map<String, List<Task>>> = _tasksByEvent

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadTasksForEvent(token: String, eventId: String, onError: ((String?) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val authHeader = "Bearer $token"
                val tasks = RetrofitInstance.api.getTasksForEvent(authHeader, eventId)

                // Mettre à jour la map en conservant les autres events
                val current = _tasksByEvent.value.toMutableMap()
                current[eventId] = tasks
                _tasksByEvent.value = current
                _error.value = null
                onError?.invoke(null)
            } catch (e: Exception) {
                val msg = e.message ?: "Erreur lors du chargement des tâches"
                _error.value = msg
                onError?.invoke(msg)
            }
        }
    }

    fun clearTasksForEvent(eventId: String) {
        val current = _tasksByEvent.value.toMutableMap()
        current.remove(eventId)
        _tasksByEvent.value = current
    }

    fun updateTaskStatus(
        token: String,
        task: Task,
        newStatus: String
    ) {
        viewModelScope.launch {
            try {
                val auth = "Bearer $token"

                val request = TaskRequest(
                    title = task.title,
                    description = task.description,
                    status = newStatus,
                    assignedTo = task.assignedTo?._id,
                    dueDate = task.dueDate
                )

                val updatedTask = RetrofitInstance.api.updateTask(
                    token = auth,
                    eventId = task.event,
                    taskId = task._id,
                    taskRequest = request
                )

                // Mise à jour locale dans tasksByEvent
                val current = _tasksByEvent.value.toMutableMap()
                val list = current[task.event]
                if (list != null) {
                    current[task.event] = list.map { if (it._id == task._id) updatedTask else it }
                    _tasksByEvent.value = current
                }

                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors de la mise à jour de la tâche"
            }
        }
    }
    fun clearAll() {
        _tasksByEvent.value = emptyMap()
        _error.value = null
    }
}
