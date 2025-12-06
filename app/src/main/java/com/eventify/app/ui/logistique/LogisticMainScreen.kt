package com.eventify.app.ui.logistic
import LogisticEventDetailScreen
import NotificationViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eventify.app.model.Event
import com.eventify.app.model.User
import com.eventify.app.ui.participant.DrawerMenuItem
import com.eventify.app.viewmodel.ChatbotViewModel
import com.eventify.app.viewmodel.EventViewModel
import com.eventify.app.viewmodel.MessageViewModel
import com.eventify.app.viewmodel.TaskViewModel
import kotlinx.coroutines.launch

sealed class LogisticScreen {
    object Home : LogisticScreen()
    object Events : LogisticScreen()
    object MyTasks : LogisticScreen()
    object Notifications : LogisticScreen()
    object Messages : LogisticScreen()
    object Chatbot : LogisticScreen()
    object Profile : LogisticScreen()
    data class EventDetail(val event: Event) : LogisticScreen()

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogisticMainScreen(
    user: User,                // contient _id, name, email, role="logistique"
    token: String,
    eventViewModel: EventViewModel,
    taskViewModel: TaskViewModel,
    messageViewModel: MessageViewModel,
    chatbotViewModel: ChatbotViewModel,
    users: List<User>,         // pour la messagerie
    onLogout: () -> Unit,
    notificationViewModel: NotificationViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf<LogisticScreen>(LogisticScreen.Home) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
                // Header
                DrawerHeaderLogistic(
                    userName = user.name,
                    userEmail = user.email,
                    onProfileClick = {
                        currentScreen = LogisticScreen.Profile
                        scope.launch { drawerState.close() }
                    }
                )

                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                // Accueil (type 1 = events, type 2 = tâches)
                DrawerMenuItem(
                    icon = Icons.Default.Home,
                    label = "Accueil",
                    selected = currentScreen is LogisticScreen.Home,
                    onClick = {
                        currentScreen = LogisticScreen.Home
                        scope.launch { drawerState.close() }
                    }
                )

                // Mes événements (seul. pertinent pour logistique type 1)
                DrawerMenuItem(
                    icon = Icons.Default.Event,
                    label = "Mes événements",
                    selected = currentScreen is LogisticScreen.Events,
                    onClick = {
                        currentScreen = LogisticScreen.Events
                        scope.launch { drawerState.close() }
                    }
                )

                // Mes tâches (type 1 et 2)
                DrawerMenuItem(
                    icon = Icons.Default.ListAlt,
                    label = "Mes tâches",
                    selected = currentScreen is LogisticScreen.MyTasks,
                    onClick = {
                        currentScreen = LogisticScreen.MyTasks
                        scope.launch { drawerState.close() }
                    }
                )

                // Notifications
                DrawerMenuItem(
                    icon = Icons.Default.Notifications,
                    label = "Notifications",
                    selected = currentScreen is LogisticScreen.Notifications,
                    onClick = {
                        currentScreen = LogisticScreen.Notifications
                        scope.launch { drawerState.close() }
                    }
                )

                // Messagerie interne
                DrawerMenuItem(
                    icon = Icons.AutoMirrored.Filled.Chat,
                    label = "Messagerie",
                    selected = currentScreen is LogisticScreen.Messages,
                    onClick = {
                        currentScreen = LogisticScreen.Messages
                        scope.launch { drawerState.close() }
                    }
                )

                // Chatbot IA
                DrawerMenuItem(
                    icon = Icons.Default.SmartToy,
                    label = "Assistant IA",
                    selected = currentScreen is LogisticScreen.Chatbot,
                    onClick = {
                        currentScreen = LogisticScreen.Chatbot
                        scope.launch { drawerState.close() }
                    }
                )

                // Profil
                DrawerMenuItem(
                    icon = Icons.Default.Person,
                    label = "Profil",
                    selected = currentScreen is LogisticScreen.Profile,
                    onClick = {
                        currentScreen = LogisticScreen.Profile
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
        when (val screen = currentScreen) {
            is LogisticScreen.Home -> LogisticHomeScreen(
                token = token,
                userId = user._id,
                onMenuClick = { scope.launch { drawerState.open() } },
                onEventClick = { ev -> currentScreen = LogisticScreen.EventDetail(ev) },
                eventViewModel = eventViewModel,
                taskViewModel = taskViewModel
            )

            is LogisticScreen.Events -> LogisticEventsScreen(
                eventViewModel = eventViewModel,
                token = token,
                userId = user._id,
                onMenuClick = { scope.launch { drawerState.open() } },
                onEventClick = { ev -> currentScreen = LogisticScreen.EventDetail(ev) }
            )

            is LogisticScreen.MyTasks -> LogisticMyTasksScreen(
                token = token,
                userId = user._id,
                taskViewModel = taskViewModel,
                onMenuClick = { scope.launch { drawerState.open() } }
            )

            is LogisticScreen.Notifications -> LogisticNotificationsScreen(
                token = token,
                userId = user._id,
                notificationViewModel = notificationViewModel,
                onMenuClick = { scope.launch { drawerState.open() } }
            )

            is LogisticScreen.Messages -> LogisticMessagesScreen(
                token = token,
                userId = user._id,
                messageViewModel = messageViewModel,
                users = users,
                onMenuClick = { scope.launch { drawerState.open() } }
            )

            is LogisticScreen.Chatbot -> LogisticChatbotScreen(
                token = token,
                chatbotViewModel = chatbotViewModel,
                onMenuClick = { scope.launch { drawerState.open() } }
            )

            is LogisticScreen.Profile -> LogisticProfileScreen(
                userName = user.name,
                userEmail = user.email,
                onMenuClick = { scope.launch { drawerState.open() } }
            )

            is LogisticScreen.EventDetail -> LogisticEventDetailScreen(
                token = token,
                user = user,
                event = screen.event,
                eventViewModel = eventViewModel,
                taskViewModel = taskViewModel,
                onBack = { currentScreen = LogisticScreen.Events }
            )
        }
    }
}

@Composable
fun DrawerHeaderLogistic(
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
                text = "Logistique",
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

