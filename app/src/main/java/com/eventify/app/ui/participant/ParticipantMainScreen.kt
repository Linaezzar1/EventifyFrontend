package com.eventify.app.ui.participant

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eventify.app.model.Event
import com.eventify.app.model.User
import com.eventify.app.viewmodel.EventViewModel
import com.eventify.app.viewmodel.ChatbotViewModel
import com.eventify.app.viewmodel.MessageViewModel
import com.eventify.app.network.RetrofitInstance
import com.eventify.app.ui.logistic.DrawerMenuItem
import kotlinx.coroutines.launch

sealed class ParticipantScreen {
    object Events : ParticipantScreen()
    object Profile : ParticipantScreen()
    object Chatbot : ParticipantScreen()
    object Messages : ParticipantScreen()
    data class EventDetail(val event: Event) : ParticipantScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantMainScreen(
    userName: String,
    userEmail: String,
    userId: String,
    userToken: String,
    eventViewModel: EventViewModel,
    onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf<ParticipantScreen>(ParticipantScreen.Events) }

    // ViewModels pour les fonctionnalités
    val chatbotViewModel = remember { ChatbotViewModel() }
    val messageViewModel = remember { MessageViewModel() }

    // Liste des utilisateurs pour la messagerie
    var users by remember { mutableStateOf<List<User>>(emptyList()) }

    // Charger les utilisateurs au démarrage
    LaunchedEffect(Unit) {
        try {
            users = RetrofitInstance.api.getAllUsers("Bearer $userToken")
        } catch (e: Exception) {
            // Ignorer l'erreur
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp)
            ) {
                // Header du Drawer avec profil
                DrawerHeader(
                    userName = userName,
                    userEmail = userEmail,
                    onProfileClick = {
                        currentScreen = ParticipantScreen.Profile
                        scope.launch { drawerState.close() }
                    }
                )

                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                // Menu items
                DrawerMenuItem(
                    icon = Icons.Default.Event,
                    label = "Événements",
                    selected = currentScreen is ParticipantScreen.Events,
                    onClick = {
                        currentScreen = ParticipantScreen.Events
                        scope.launch { drawerState.close() }
                    }
                )

                DrawerMenuItem(
                    icon = Icons.Default.SmartToy,
                    label = "Chatbot IA",
                    selected = currentScreen is ParticipantScreen.Chatbot,
                    onClick = {
                        currentScreen = ParticipantScreen.Chatbot
                        scope.launch { drawerState.close() }
                    }
                )

                DrawerMenuItem(
                    icon = Icons.AutoMirrored.Filled.Chat,
                    label = "Messagerie",
                    selected = currentScreen is ParticipantScreen.Messages,
                    onClick = {
                        currentScreen = ParticipantScreen.Messages
                        scope.launch { drawerState.close() }
                    }
                )

                Spacer(modifier = Modifier.weight(1f))

                HorizontalDivider()

                DrawerMenuItem(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    label = "Déconnexion",
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    },
                    tint = Color.Red
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        // Contenu principal
        when (val screen = currentScreen) {
            is ParticipantScreen.Events -> ParticipantEventsScreen(
                eventViewModel = eventViewModel,
                token = userToken,
                userId = userId,
                userEmail = userEmail,
                onMenuClick = { scope.launch { drawerState.open() } },
                onEventClick = { event -> currentScreen = ParticipantScreen.EventDetail(event) }
            )
            is ParticipantScreen.Profile -> ParticipantProfileScreen(
                userName = userName,
                userEmail = userEmail,
                onMenuClick = { scope.launch { drawerState.open() } },
                onBack = { currentScreen = ParticipantScreen.Events }
            )
            is ParticipantScreen.Chatbot -> ParticipantChatbotScreen(
                token = userToken,
                chatbotViewModel = chatbotViewModel,
                onMenuClick = { scope.launch { drawerState.open() } }
            )
            is ParticipantScreen.Messages -> ParticipantMessagesScreen(
                token = userToken,
                userId = userId,
                messageViewModel = messageViewModel,
                users = users,
                onMenuClick = { scope.launch { drawerState.open() } }
            )
            is ParticipantScreen.EventDetail -> ParticipantEventDetailScreen(
                event = screen.event,
                userId = userId,
                userEmail = userEmail,
                token = userToken,
                eventViewModel = eventViewModel,
                onBack = { currentScreen = ParticipantScreen.Events }
            )
        }
    }
}

@Composable
fun DrawerHeader(
    userName: String,
    userEmail: String,
    onProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2196F3))
            .padding(24.dp)
            .clickable { onProfileClick() }
    ) {
        Column {
            // Avatar avec icône de profil
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFF2196F3)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = userName,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = userEmail,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Participant",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    tint: Color = Color(0xFF2196F3)
) {
    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) tint else Color.Gray
            )
        },
        label = {
            Text(
                text = label,
                color = if (selected) tint else Color.DarkGray,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = tint.copy(alpha = 0.1f),
            unselectedContainerColor = Color.Transparent
        )
    )
}

