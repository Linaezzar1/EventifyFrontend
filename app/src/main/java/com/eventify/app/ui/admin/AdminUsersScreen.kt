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
import com.eventify.app.model.User
import com.eventify.app.viewmodel.AdminViewModel
import com.eventify.app.viewmodel.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersManagementScreen(
    adminViewModel: AdminViewModel,
    eventViewModel: EventViewModel,
    token: String,
    onBack: () -> Unit,
    filterByRole: String? = null
) {
    val users by adminViewModel.users.collectAsState()
    val events by eventViewModel.events.collectAsState()
    val loading by adminViewModel.loading.collectAsState()
    var error by remember { mutableStateOf<String?>(null) }
    
    // Filter users by role if specified
    // For "participant", show all users (since participants are users who joined events)
    val filteredUsers = remember(users, filterByRole) {
        when (filterByRole?.lowercase()) {
            "participant" -> users // Show all users for participants
            "organisateur" -> users.filter { it.role.lowercase() == "organisateur" }
            else -> users
        }
    }

    LaunchedEffect(Unit) {
        eventViewModel.loadEvents(token) { err ->
            error = err
        }
    }
    
    LaunchedEffect(events) {
        if (events.isNotEmpty()) {
            adminViewModel.collectUsersFromEvents(events, token) { err ->
                error = err
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        when (filterByRole?.lowercase()) {
                            "organisateur" -> "Organisateurs"
                            else -> if (filterByRole == null) "Gestion des utilisateurs" else "Participants"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (loading && users.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF2196F3))
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

                if (filteredUsers.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Aucun utilisateur trouvé")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredUsers) { user ->
                            UserItem(user = user)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserItem(
    user: User
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A237E)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666)
                )
                Spacer(modifier = Modifier.height(4.dp))
                RoleChip(role = user.role)
            }
        }
    }
}

@Composable
fun RoleChip(role: String) {
    val roleColor = when (role.lowercase()) {
        "admin" -> Color(0xFFD32F2F)
        "organisateur" -> Color(0xFFFF9800)
        "logistique" -> Color(0xFF2196F3)
        "communication" -> Color(0xFF4CAF50)
        else -> Color(0xFF9E9E9E)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = roleColor.copy(alpha = 0.2f),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Text(
            text = role,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = roleColor,
            fontWeight = FontWeight.Medium
        )
    }
}
