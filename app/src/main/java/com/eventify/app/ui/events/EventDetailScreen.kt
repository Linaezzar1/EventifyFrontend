package com.eventify.app.ui.events

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eventify.app.model.Event
import com.eventify.app.model.User
import com.eventify.app.viewmodel.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    event: Event,
    userRole: String,
    userEmail: String,
    userId: String,
    token: String,
    eventViewModel: EventViewModel,
    onBack: () -> Unit,
    onEventDeleted: () -> Unit,
    onEditEvent: () -> Unit,
    onShowTasks: (String) -> Unit  // Ajout du paramètre pour afficher gestion tâche
) {
    var showConfirmation by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var isParticipant by remember { mutableStateOf(event.participants?.contains(userId) ?: false) }
    var showParticipants by remember { mutableStateOf(false) }
    var participants by remember { mutableStateOf<List<User>>(emptyList()) }
    var roleFilter by remember { mutableStateOf<String?>(null) }

    // Filtrage avancé participants
    val rolesList = listOf("Tous") + participants.map { it.role }.distinct()
    val filteredParticipants = if (roleFilter.isNullOrEmpty() || roleFilter == "Tous") {
        participants
    } else {
        participants.filter { it.role == roleFilter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event.title) },
                actions = {
                    // Boutons organisateur
                    if (userRole == "organisateur" && event.createdBy?.email == userEmail) {
                        IconButton(onClick = { onEditEvent() }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Modifier")
                        }
                        IconButton(onClick = { showConfirmation = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Supprimer")
                        }
                        IconButton(onClick = {
                            showParticipants = true
                            eventViewModel.loadParticipants(token, event._id,
                                onSuccess = { participants = it },
                                onError = { error = it }
                            )
                        }) {
                            Icon(Icons.Filled.Group, contentDescription = "Participants")
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Description:", style = MaterialTheme.typography.titleMedium)
            Text(event.description ?: "Pas de description", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            Text("Date: ${event.date}", style = MaterialTheme.typography.bodyMedium)
            event.location?.let {
                Text("Lieu: $it", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(16.dp))
            event.createdBy?.let {
                Text("Créé par: ${it.name} (${it.role})", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(24.dp))

            // Tous sauf organisateur peuvent s'inscrire
            if (userRole != "organisateur") {
                Button(
                    onClick = {
                        if (isParticipant) {
                            eventViewModel.leaveEvent(token, event._id,
                                onSuccess = { isParticipant = false },
                                onError = { error = it }
                            )
                        } else {
                            eventViewModel.joinEvent(token, event._id,
                                onSuccess = { isParticipant = true },
                                onError = { error = it }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isParticipant) Color.Red else Color(0xFFFF9800)
                    )
                ) {
                    Text(if (isParticipant) "Se désinscrire" else "S'inscrire", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bouton gestion tâches (visible pour certains rôles)
            if (userRole == "organisateur" || userRole == "logistique" || userRole == "communication") {
                Button(
                    onClick = { onShowTasks(event._id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Gérer les tâches")
                }
            }

            error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = Color.Red)
            }
        }
    }

    // Dialog suppression
    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text("Confirmer la suppression") },
            text = { Text("Êtes-vous sûr de vouloir supprimer cet événement ?") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmation = false
                    eventViewModel.deleteEvent(token, event._id,
                        onSuccess = { onEventDeleted() },
                        onError = { error = it }
                    )
                }) { Text("Supprimer", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmation = false }) { Text("Annuler") }
            }
        )
    }

    // Dialog participants avec filtre rôle
    if (showParticipants) {
        AlertDialog(
            onDismissRequest = { showParticipants = false },
            title = { Text("Participants (${filteredParticipants.size})") },
            text = {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Filtrer : ", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.width(8.dp))
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(onClick = { expanded = true }) {
                                Text(roleFilter ?: "Tous")
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                rolesList.forEach { role ->
                                    DropdownMenuItem(
                                        text = { Text(role) },
                                        onClick = {
                                            roleFilter = role
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    filteredParticipants.forEach { user ->
                        Text("${user.name} (${user.role}) - ${user.email}")
                        Spacer(Modifier.height(8.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showParticipants = false }) { Text("Fermer") }
            }
        )
    }
}
