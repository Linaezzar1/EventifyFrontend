package com.eventify.app.ui.logistic

import NotificationViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eventify.app.model.Notification
import com.eventify.app.model.NotificationType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogisticNotificationsScreen(
    token: String,
    userId: String,
    notificationViewModel: NotificationViewModel,
    onMenuClick: () -> Unit
) {
    val notifications by notificationViewModel.notifications.collectAsState()
    val isLoading by notificationViewModel.isLoading.collectAsState()
    val error by notificationViewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        notificationViewModel.loadNotifications(token)
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF2196F3))
                    }
                }

                error?.isNotBlank() == true -> {
                    Text(
                        text = error ?: "Erreur",
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }

                notifications.isEmpty() -> {
                    Text(
                        text = "Aucune notification",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    // Tri par date décroissante si createdAt est ISO 8601
                    val sorted = notifications.sortedByDescending { it.createdAt ?: "" }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sorted) { notif ->
                            LogisticNotificationItem(
                                notification = notif,
                                onClick = {
                                    notificationViewModel.markAsRead(token, notif._id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogisticNotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    val (title, accentColor, bgColor) = when (notification.type) {
        NotificationType.RAPPEL_TACHE ->
            Triple("Rappel de tâche", Color(0xFF2196F3), Color(0xFFE3F2FD))
        NotificationType.ALERTE_RETARD ->
            Triple("Alerte retard", Color(0xFFF44336), Color(0xFFFFEBEE))
        NotificationType.CHANGEMENT_HORAIRE ->
            Triple("Changement d'horaire", Color(0xFFFF9800), Color(0xFFFFF3E0))
        NotificationType.VALIDATION_INSCRIPTION ->
            Triple("Validation d'inscription", Color(0xFF4CAF50), Color(0xFFE8F5E9))
        else ->
            Triple("Notification", Color(0xFF607D8B), Color(0xFFECEFF1))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    color = accentColor,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                if (!notification.read) {
                    AssistChip(
                        onClick = onClick,
                        label = { Text("Nouveau", color = Color.White) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = accentColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = notification.message ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(4.dp))

            notification.createdAt?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}
