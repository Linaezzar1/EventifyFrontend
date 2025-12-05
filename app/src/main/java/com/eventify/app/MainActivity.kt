package com.eventify.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.eventify.app.ui.auth.LoginScreen
import com.eventify.app.ui.auth.SignupScreen
import com.eventify.app.ui.events.EventDetailScreen
import com.eventify.app.ui.events.EventsListScreen
import com.eventify.app.ui.events.CreateEventScreen
import com.eventify.app.ui.theme.EventifyTheme
import com.eventify.app.ui.events.EditEventScreen
import com.eventify.app.ui.tasks.TaskListScreen
import com.eventify.app.ui.participant.ParticipantMainScreen
import com.eventify.app.viewmodel.EventViewModel
import com.eventify.app.model.Event

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EventifyTheme {
                var isLoggedIn by remember { mutableStateOf(false) }
                var isSignup by remember { mutableStateOf(false) }
                var userName by remember { mutableStateOf("") }
                var userToken by remember { mutableStateOf("") }
                var userRole by remember { mutableStateOf("") }
                var userEmail by remember { mutableStateOf("") }
                var userId by remember { mutableStateOf("") }
                val eventViewModel = remember { EventViewModel() }
                var selectedEvent by remember { mutableStateOf<Event?>(null) }
                var isCreatingEvent by remember { mutableStateOf(false) }
                var isEditingEvent by remember { mutableStateOf(false) }
                var showingTasksForEventId by remember { mutableStateOf<String?>(null) }

                when {
                    !isLoggedIn && !isSignup -> LoginScreen(
                        onLoginSuccess = { token, role, name, email, id ->
                            isLoggedIn = true
                            userToken = token
                            userRole = role
                            userName = name
                            userEmail = email
                            userId = id
                        },
                        onSwitchToSignup = { isSignup = true }
                    )
                    !isLoggedIn && isSignup -> SignupScreen(
                        onSignupSuccess = { name ->
                            isLoggedIn = true
                            userName = name
                        },
                        onSwitchToLogin = { isSignup = false }
                    )
                    // Interface Participant avec Drawer
                    isLoggedIn && userRole.trim().lowercase() == "participant" -> ParticipantMainScreen(
                        userName = userName,
                        userEmail = userEmail,
                        userId = userId,
                        userToken = userToken,
                        eventViewModel = eventViewModel,
                        onLogout = {
                            isLoggedIn = false
                            isSignup = false
                            userToken = ""
                            userRole = ""
                            userName = ""
                            userEmail = ""
                            userId = ""
                        }
                    )
                    // Interface pour les autres rôles (organisateur, etc.)
                    isCreatingEvent -> CreateEventScreen(
                        eventViewModel = eventViewModel,
                        token = userToken,
                        onEventCreated = {
                            isCreatingEvent = false
                            eventViewModel.loadEvents(userToken) { }
                        }
                    )
                    isEditingEvent && selectedEvent != null -> EditEventScreen(
                        event = selectedEvent!!,
                        eventViewModel = eventViewModel,
                        token = userToken,
                        onEditFinished = {
                            isEditingEvent = false
                            selectedEvent = null
                            eventViewModel.loadEvents(userToken) { }
                        }
                    )
                    showingTasksForEventId != null -> TaskListScreen(
                        eventId = showingTasksForEventId!!,
                        token = userToken,
                        eventViewModel = eventViewModel,
                        onBack = { showingTasksForEventId = null }
                    )
                    selectedEvent != null -> EventDetailScreen(
                        event = selectedEvent!!,
                        userRole = userRole,
                        userEmail = userEmail,
                        userId = userId,
                        token = userToken,
                        eventViewModel = eventViewModel,
                        onBack = { selectedEvent = null },
                        onEventDeleted = {
                            selectedEvent = null
                            eventViewModel.loadEvents(userToken) { }
                        },
                        onEditEvent = { isEditingEvent = true },
                        onShowTasks = { eventId -> showingTasksForEventId = eventId } // Nouvel argument
                    )
                    else -> EventsListScreen(
                        eventViewModel = eventViewModel,
                        token = userToken,
                        userRole = userRole,
                        onEventClick = { event -> selectedEvent = event },
                        onCreateEventClick = { isCreatingEvent = true },
                        onLogout = {
                            isLoggedIn = false
                            isSignup = false
                            userToken = ""
                            userRole = ""
                            userName = ""
                            userEmail = ""
                            userId = ""
                        }
                    )
                }
            }
        }
    }
}
