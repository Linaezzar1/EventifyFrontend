package com.eventify.app.ui.admin

import androidx.compose.foundation.layout.*
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
import com.eventify.app.model.AdminStats
import com.eventify.app.viewmodel.AdminViewModel
import com.eventify.app.viewmodel.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    adminViewModel: AdminViewModel,
    eventViewModel: EventViewModel,
    token: String,
    onBack: () -> Unit
) {
    val users by adminViewModel.users.collectAsState()
    val events by eventViewModel.events.collectAsState()
    val loading by adminViewModel.loading.collectAsState()

    LaunchedEffect(Unit) {
        eventViewModel.loadEvents(token) { }
    }
    
    LaunchedEffect(events) {
        if (events.isNotEmpty()) {
            adminViewModel.collectUsersFromEvents(events, token) { }
        }
    }
    
    // Calculate stats from available data
    val displayStats = remember(users, events) {
        val uniqueUserIds = mutableSetOf<String>()
        events.forEach { event ->
            event.participants?.forEach { userId ->
                uniqueUserIds.add(userId)
            }
        }
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
                title = { Text("Statistiques") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),
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
                CircularProgressIndicator(color = Color(0xFF4CAF50))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Overall Statistics
                Text(
                    text = "Vue d'ensemble",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A237E)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Utilisateurs",
                        value = displayStats.totalUsers.toString(),
                        icon = Icons.Default.People,
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF2196F3)
                    )
                    StatCard(
                        title = "Total Événements",
                        value = displayStats.totalEvents.toString(),
                        icon = Icons.Default.Event,
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFFF9800)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Participants",
                        value = displayStats.totalParticipants.toString(),
                        icon = Icons.Default.Group,
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF4CAF50)
                    )
                }

                // Users by Role
                displayStats.usersByRole?.let { usersByRole ->
                    if (usersByRole.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Utilisateurs par rôle",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A237E)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                usersByRole.forEach { (role, count) ->
                                    RoleStatRow(role = role, count = count)
                                    if (role != usersByRole.keys.last()) {
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoleStatRow(role: String, count: Int) {
    val roleColor = when (role.lowercase()) {
        "admin" -> Color(0xFFD32F2F)
        "organisateur" -> Color(0xFFFF9800)
        "logistique" -> Color(0xFF2196F3)
        "communication" -> Color(0xFF4CAF50)
        "participant" -> Color(0xFF9E9E9E)
        else -> Color(0xFF9E9E9E)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = roleColor.copy(alpha = 0.2f)
            ) {
                Text(
                    text = role.replaceFirstChar { it.uppercaseChar() },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = roleColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A237E)
        )
    }
}

