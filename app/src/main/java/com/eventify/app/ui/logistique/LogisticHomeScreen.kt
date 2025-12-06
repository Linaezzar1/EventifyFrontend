package com.eventify.app.ui.logistic

import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eventify.app.model.Event
import com.eventify.app.model.Task
import com.eventify.app.viewmodel.EventViewModel
import com.eventify.app.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogisticHomeScreen(
    token: String,
    userId: String,
    onMenuClick: () -> Unit,
    onEventClick: (Event) -> Unit,
    eventViewModel: EventViewModel,
    taskViewModel: TaskViewModel
) {
    val events by eventViewModel.events.collectAsState()
    val tasksByEvent by taskViewModel.tasksByEvent.collectAsState()
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        eventViewModel.loadEvents(token) { err -> error = err }
    }

    val myEvents = events.filter { ev ->
        ev.logisticManager == userId || ev.logisticStaff?.contains(userId) == true
    }

    LaunchedEffect(myEvents) {
        myEvents.forEach { ev ->
            taskViewModel.loadTasksForEvent(token, ev._id)
        }
    }

    val now = remember { java.time.LocalDate.now() }
    val myTodayTasks = mutableListOf<Task>()
    val myOverdueTasks = mutableListOf<Task>()

    myEvents.forEach { ev ->
        val list = tasksByEvent[ev._id] ?: emptyList()
        list.filter { it.assignedTo?._id == userId }.forEach { task ->
            val due = task.dueDate
            if (due != null) {
                val date = java.time.LocalDate.parse(due.substring(0, 10))
                when {
                    task.status == "en_retard" ||
                            (date.isBefore(now) && task.status != "termine") ->
                        myOverdueTasks.add(task)

                    date.isEqual(now) && task.status != "termine" ->
                        myTodayTasks.add(task)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Accueil logistique") },
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
            error?.let { if (it.isNotBlank()) Text(it, color = Color.Red) }

            Text(
                text = "Résumé de vos tâches",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryChip("Aujourd'hui", myTodayTasks.size.toString(), Color(0xFF4CAF50))
                SummaryChip("En retard", myOverdueTasks.size.toString(), Color(0xFFF44336))
                SummaryChip("Événements", myEvents.size.toString(), Color(0xFF2196F3))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Événements où vous êtes logistique",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (myEvents.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aucun événement avec rôle logistique", color = Color.Gray)
                }
            } else {
                LazyColumn {
                    items(myEvents) { ev ->
                        val list = tasksByEvent[ev._id] ?: emptyList()
                        val myTasksForEvent = list.filter { it.assignedTo?._id == userId }
                        val overdue = myTasksForEvent.count { it.status == "en_retard" }

                        LogisticHomeEventItem(
                            event = ev,
                            myTasksCount = myTasksForEvent.size,
                            overdueCount = overdue,
                            onClick = { onEventClick(ev) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryChip(label: String, value: String, color: Color) {
    AssistChip(
        onClick = { },
        label = { Text("$label : $value", color = Color.White) },
        colors = AssistChipDefaults.assistChipColors(containerColor = color)
    )
}

@Composable
fun LogisticHomeEventItem(
    event: Event,
    myTasksCount: Int,
    overdueCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF2196F3),
                    fontWeight = FontWeight.Bold
                )
                if (overdueCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFF44336)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = overdueCount.toString(),
                            color = Color(0xFFF44336),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = event.description ?: "Pas de description", color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("📅 ${event.date}", style = MaterialTheme.typography.bodySmall)
                Text("Tâches : $myTasksCount", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
