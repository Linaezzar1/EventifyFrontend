package com.eventify.app.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eventify.app.model.Event
import com.eventify.app.viewmodel.AdminViewModel
import com.eventify.app.viewmodel.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsManagementScreen(
    adminViewModel: AdminViewModel,
    eventViewModel: EventViewModel,
    token: String,
    onBack: () -> Unit,
    onEventClick: (Event) -> Unit
) {
    val events by eventViewModel.events.collectAsState()
    val loading by adminViewModel.loading.collectAsState()
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        eventViewModel.loadEvents(token) { err ->
            error = err
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestion des événements") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFF9800),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (loading && events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFF9800))
            }
        } else {
            Column(modifier = Modifier.padding(padding)) {
                error?.let {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                if (events.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Aucun événement trouvé")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(events) { event ->
                            ManagementEventItem(
                                event = event,
                                onClick = { onEventClick(event) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManagementEventItem(
    event: Event,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2196F3)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.description ?: "Pas de description",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📅 ${event.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF999999)
                    )
                    event.location?.let {
                        Text(
                            text = "📍 $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF999999)
                        )
                    }
                    event.participants?.size?.let { size ->
                        Text(
                            text = "👥 $size",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF999999)
                        )
                    }
                }
                event.createdBy?.let { creator ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Créé par: ${creator.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF999999)
                    )
                }
            }
        }
    }
}

