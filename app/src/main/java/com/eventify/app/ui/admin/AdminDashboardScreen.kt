package com.eventify.app.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eventify.app.model.AdminStats
import com.eventify.app.viewmodel.AdminViewModel
import com.eventify.app.viewmodel.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    adminViewModel: AdminViewModel,
    eventViewModel: EventViewModel,
    token: String,
    userRole: String,
    onLogout: () -> Unit,
    onBack: () -> Unit = {},
    onNavigateToUsers: () -> Unit = {},
    onNavigateToEvents: () -> Unit = {},
    onNavigateToParticipants: () -> Unit = {},
    onNavigateToOrganisateurs: () -> Unit = {}
) {
    // Check if user has organizateur role
    if (userRole.trim().lowercase() != "organisateur") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Accès refusé",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Seuls les organisateurs peuvent accéder au tableau de bord.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBack) {
                Text("Retour")
            }
        }
        return
    }
    val users by adminViewModel.users.collectAsState()
    val events by eventViewModel.events.collectAsState()
    val loading by adminViewModel.loading.collectAsState()
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        eventViewModel.loadEvents(token) { err ->
            error = err
        }
    }
    
    // Collect users from events after events are loaded
    LaunchedEffect(events) {
        if (events.isNotEmpty()) {
            adminViewModel.collectUsersFromEvents(events, token) { err ->
                error = err
            }
        }
    }
    
    // Calculate stats from available data (events and participants)
    val displayStats = remember(users, events) {
        // Get unique user count from participants across all events
        val uniqueUserIds = mutableSetOf<String>()
        events.forEach { event ->
            event.participants?.forEach { userId ->
                uniqueUserIds.add(userId)
            }
        }
        
        // Also count users we've loaded from participant details
        users.forEach { user ->
            uniqueUserIds.add(user._id)
        }
        
        val totalUsers = uniqueUserIds.size
        val totalEvents = events.size
        val totalParticipants = events.sumOf { it.participants?.size ?: 0 }
        val usersByRole = users.groupBy { it.role }.mapValues { it.value.size }
        
        AdminStats(
            totalUsers = totalUsers,
            totalEvents = totalEvents,
            totalParticipants = totalParticipants,
            usersByRole = usersByRole
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tableau de bord") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3),
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Déconnexion", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        if (loading && displayStats == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF2196F3))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Show error if any (but don't block the view)
                error?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Text(
                            text = "Note: ${it}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                // Statistics Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Utilisateurs",
                        value = displayStats.totalUsers.toString(),
                        icon = Icons.Default.People,
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF2196F3),
                        onClick = onNavigateToUsers
                    )
                    StatCard(
                        title = "Événements",
                        value = displayStats.totalEvents.toString(),
                        icon = Icons.Default.Event,
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFFF9800),
                        onClick = onNavigateToEvents
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Participants",
                        value = displayStats.totalParticipants.toString(),
                        icon = Icons.Default.Group,
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF4CAF50),
                        onClick = onNavigateToParticipants
                    )
                    StatCard(
                        title = "Organisateurs",
                        value = displayStats.usersByRole?.get("organisateur")?.toString() ?: "0",
                        icon = Icons.Default.Person,
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF9C27B0),
                        onClick = onNavigateToOrganisateurs
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF1A237E)
            )
        }
    }
}
