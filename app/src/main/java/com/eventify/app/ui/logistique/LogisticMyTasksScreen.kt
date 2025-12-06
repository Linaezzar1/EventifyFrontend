package com.eventify.app.ui.logistic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eventify.app.model.Task
import com.eventify.app.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogisticMyTasksScreen(
    token: String,
    userId: String,
    taskViewModel: TaskViewModel,
    onMenuClick: () -> Unit
) {
    val tasksByEvent by taskViewModel.tasksByEvent.collectAsState()
    val error by taskViewModel.error.collectAsState()

    var statusFilter by remember { mutableStateOf<TaskStatusFilter>(TaskStatusFilter.ALL) }

    // Aplatir toutes les tâches de tous les events et garder celles assignées à l'utilisateur
    val allMyTasks = tasksByEvent.values
        .flatten()
        .filter { it.assignedTo?._id == userId }

    val filteredTasks = allMyTasks.filter { task ->
        when (statusFilter) {
            TaskStatusFilter.ALL -> true
            TaskStatusFilter.TODO -> task.status == "a_faire"
            TaskStatusFilter.IN_PROGRESS -> task.status == "en_cours"
            TaskStatusFilter.DONE -> task.status == "termine"
            TaskStatusFilter.OVERDUE -> task.status == "en_retard"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes tâches logistiques") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Filtres par statut
            TaskStatusFilters(
                current = statusFilter,
                onChange = { statusFilter = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Erreur éventuelle
            error?.let { err ->
                if (err.isNotBlank()) {
                    Text(
                        text = err,
                        color = Color.Red,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucune tâche trouvée",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn {
                    items(filteredTasks) { task ->
                        LogisticTaskItem(
                            task = task,
                            onStatusChange = { newStatus ->
                                taskViewModel.updateTaskStatus(
                                    token = token,
                                    task = task,
                                    newStatus = newStatus
                                )
                            }
                        )
                    }
                }


            }
        }
    }
}

enum class TaskStatusFilter(val label: String) {
    ALL("Toutes"),
    TODO("À faire"),
    IN_PROGRESS("En cours"),
    DONE("Terminées"),
    OVERDUE("En retard")
}

@Composable
fun TaskStatusFilters(
    current: TaskStatusFilter,
    onChange: (TaskStatusFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TaskStatusFilter.values().forEach { filter ->
            AssistChip(
                onClick = { onChange(filter) },
                label = {
                    Text(
                        text = filter.label,
                        color = if (current == filter) Color.White else Color(0xFF2196F3)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (current == filter) Color(0xFF2196F3) else Color.Transparent
                )
            )
        }
    }
}

@Composable
fun LogisticTaskItem(
    task: Task,
    onStatusChange: (String) -> Unit
) {
    val isOverdue = task.status == "en_retard"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )

                if (isOverdue) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "En retard",
                            tint = Color(0xFFF44336)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "En retard",
                            color = Color(0xFFF44336),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            task.description?.let {
                if (it.isNotBlank()) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                task.dueDate?.let { due ->
                    Text(
                        text = "🕒 $due",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }

                Text(
                    text = "Événement : ${task.event}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Choix du statut
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusChipSimple(
                    label = "À faire",
                    selected = task.status == "en_attente",
                    onClick = { onStatusChange("en_attente") }
                )
                StatusChipSimple(
                    label = "En cours",
                    selected = task.status == "en_cours",
                    onClick = { onStatusChange("en_cours") }
                )
                StatusChipSimple(
                    label = "Terminée",
                    selected = task.status == "termine",
                    onClick = { onStatusChange("termine") }
                )
            }
        }
    }
}

@Composable
fun StatusChipSimple(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) Color(0xFF2196F3) else Color.LightGray,
            labelColor = if (selected) Color.White else Color.DarkGray
        )
    )
}

