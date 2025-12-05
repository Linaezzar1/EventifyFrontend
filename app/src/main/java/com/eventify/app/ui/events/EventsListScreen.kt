package com.eventify.app.ui.events

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eventify.app.model.Event
import com.eventify.app.viewmodel.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsListScreen(
    eventViewModel: EventViewModel,
    token: String,
    userRole: String,
    onEventClick: (Event) -> Unit,
    onCreateEventClick: () -> Unit,
    onLogout: () -> Unit,
    onDashboardClick: (() -> Unit)? = null
) {
    val events by eventViewModel.events.collectAsState()
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        eventViewModel.loadEvents(token) { err ->
            error = err
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Événements") },
                actions = {
                    if (userRole.trim() == "organisateur" && onDashboardClick != null) {
                        IconButton(onClick = { onDashboardClick() }) {
                            Icon(Icons.Default.Dashboard, contentDescription = "Tableau de bord", tint = Color.White)
                        }
                    }
                    IconButton(onClick = { onLogout() }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Déconnexion", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (userRole.trim() == "organisateur") {
                FloatingActionButton(
                    onClick = onCreateEventClick,
                    containerColor = Color(0xFFFF9800)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Créer événement", tint = Color.White)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            val errorMsg = error ?: ""
            if (
                errorMsg.isNotBlank() &&
                !errorMsg.contains("Unauthorized", ignoreCase = true) &&
                !errorMsg.contains("401", ignoreCase = true) &&
                !errorMsg.contains("403", ignoreCase = true)
            ) {
                Text(text = errorMsg, color = Color.Red)
            }
            if (events.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aucun événement disponible")
                }
            } else {
                LazyColumn {
                    items(events) { event ->
                        EventItem(event = event, onClick = { onEventClick(event) })
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun EventItem(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = event.title, style = MaterialTheme.typography.titleMedium, color = Color(0xFF2196F3))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = event.description ?: "Pas de description", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "📅 ${event.date}", style = MaterialTheme.typography.bodySmall)
            event.location?.let {
                Text(text = "📍 $it", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
