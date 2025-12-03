package com.eventify.app.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eventify.app.model.Task
import com.eventify.app.model.TaskRequest
import com.eventify.app.viewmodel.EventViewModel
import androidx.compose.material.icons.filled.ArrowBack
import com.eventify.app.model.User

fun millisToIso(millis: Long): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault())
    sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
    return sdf.format(java.util.Date(millis))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    eventId: String,
    token: String,
    eventViewModel: EventViewModel,
    onBack: () -> Unit
) {
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var currentTask by remember { mutableStateOf<Task?>(null) }
    var participants by remember { mutableStateOf<List<User>>(emptyList()) }


    // Chargement des tâches
    LaunchedEffect(eventId) {
        eventViewModel.getTasksForEvent(token, eventId,
            onSuccess = {
                tasks = it
                loading = false
            },
            onError = {
                error = it
                loading = false
            }
        )
        eventViewModel.loadParticipants(token, eventId,
            onSuccess = { users -> participants = users },
            onError = { error = error ?: "Erreur chargement participants" }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tâches") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                currentTask = null // Création nouvelle tâche
                showEditDialog = true
            }) {
                Text("+")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.fillMaxSize())
            } else if (error != null) {
                Text("Erreur : $error", color = MaterialTheme.colorScheme.error)
            } else {
                LazyColumn {
                    items(tasks) { task ->
                        TaskItem(task = task,
                            onEdit = {
                                currentTask = task
                                showEditDialog = true
                            },
                            onDelete = {
                                eventViewModel.deleteTask(token, task._id,
                                    onSuccess = {
                                        tasks = tasks.filter { it._id != task._id }
                                    },
                                    onError = { error = it }
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog édition/creation tâche
    if (showEditDialog) {
        TaskEditDialog(
            task = currentTask,
            participants = participants,
            onDismiss = { showEditDialog = false },
            onSave = { title, desc, status, assignedTo, dueDate ->
                val req = TaskRequest(title, desc, status, assignedTo, dueDate?.let { millisToIso(it) });                if (currentTask == null) {
                    eventViewModel.createTask(token, eventId, req,
                        onSuccess = {
                            showEditDialog = false
                            eventViewModel.getTasksForEvent(token, eventId,
                                onSuccess = { tasks = it },
                                onError = { error = it }
                            )
                        },
                        onError = { error = it }
                    )
                } else {
                    eventViewModel.updateTask(token, currentTask!!._id, req,
                        onSuccess = {
                            showEditDialog = false
                            eventViewModel.getTasksForEvent(token, eventId,
                                onSuccess = { tasks = it },
                                onError = { error = it }
                            )
                        },
                        onError = { error = it }
                    )
                }
            }
        )
    }
}

@Composable
fun TaskItem(task: Task, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(task.title, style = MaterialTheme.typography.titleMedium)
                Text(task.description ?: "", style = MaterialTheme.typography.bodySmall)
                Text("Statut : ${task.status}", style = MaterialTheme.typography.bodySmall)
                task.assignedTo?.let {
                    Text("Assignée à: ${it.name} (${it.role})", style = MaterialTheme.typography.bodySmall)
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "Modifier")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Supprimer")
                }
            }
        }
    }
}
