package com.eventify.app.ui.participant

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eventify.app.model.Event
import com.eventify.app.viewmodel.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantEventsScreen(
    eventViewModel: EventViewModel,
    token: String,
    userId: String,
    userEmail: String,
    onMenuClick: () -> Unit,
    onEventClick: (Event) -> Unit
) {
    val events by eventViewModel.events.collectAsState()
    var error by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyRegistered by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        eventViewModel.loadEvents(token) { err ->
            error = err
        }
    }

    // Filtrer les événements
    val filteredEvents = events.filter { event ->
        val matchesSearch = event.title.contains(searchQuery, ignoreCase = true) ||
                (event.description?.contains(searchQuery, ignoreCase = true) ?: false)

        val matchesRegistration = if (showOnlyRegistered) {
            event.participants?.contains(userId) == true
        } else true

        matchesSearch && matchesRegistration
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Événements") },
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
        ) {
            // Barre de recherche
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Rechercher un événement...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Rechercher")
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    cursorColor = Color(0xFF2196F3)
                )
            )

            // Filtres
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = !showOnlyRegistered,
                    onClick = { showOnlyRegistered = false },
                    label = { Text("Tous") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2196F3),
                        selectedLabelColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = showOnlyRegistered,
                    onClick = { showOnlyRegistered = true },
                    label = { Text("Mes inscriptions") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF4CAF50),
                        selectedLabelColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Message d'erreur
            val errorMsg = error ?: ""
            if (
                errorMsg.isNotBlank() &&
                !errorMsg.contains("Unauthorized", ignoreCase = true) &&
                !errorMsg.contains("401", ignoreCase = true) &&
                !errorMsg.contains("403", ignoreCase = true)
            ) {
                Text(
                    text = errorMsg,
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Liste des événements
            if (filteredEvents.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (showOnlyRegistered)
                            "Vous n'êtes inscrit à aucun événement"
                        else
                            "Aucun événement disponible",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn {
                    items(filteredEvents) { event ->
                        ParticipantEventItem(
                            event = event,
                            isRegistered = event.participants?.contains(userId) == true,
                            onClick = { onEventClick(event) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ParticipantEventItem(
    event: Event,
    isRegistered: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
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
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                if (isRegistered) {
                    AssistChip(
                        onClick = { },
                        label = { Text("Inscrit", color = Color.White) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = event.description ?: "Pas de description",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "📅 ${event.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )

                event.location?.let {
                    Text(
                        text = "📍 $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }
            }

            // Nombre de participants
            event.participants?.let { participants ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "👥 ${participants.size} participant(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

