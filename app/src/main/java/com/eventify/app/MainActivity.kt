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
import com.eventify.app.ui.admin.DashboardScreen
import com.eventify.app.ui.admin.UsersManagementScreen
import com.eventify.app.ui.admin.EventsManagementScreen
import com.eventify.app.ui.admin.StatsScreen
import com.eventify.app.viewmodel.EventViewModel
import com.eventify.app.viewmodel.AdminViewModel
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
                val adminViewModel = remember { AdminViewModel() }
                var showingDashboard by remember { mutableStateOf(false) }
                var dashboardScreen by remember { mutableStateOf<String?>(null) } // "users", "events", "stats"

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
                    // Dashboard screens
                    showingDashboard && dashboardScreen == "users" -> UsersManagementScreen(
                        adminViewModel = adminViewModel,
                        eventViewModel = eventViewModel,
                        token = userToken,
                        onBack = { dashboardScreen = null }
                    )
                    showingDashboard && dashboardScreen == "participants" -> UsersManagementScreen(
                        adminViewModel = adminViewModel,
                        eventViewModel = eventViewModel,
                        token = userToken,
                        onBack = { dashboardScreen = null },
                        filterByRole = "participant"
                    )
                    showingDashboard && dashboardScreen == "organisateurs" -> UsersManagementScreen(
                        adminViewModel = adminViewModel,
                        eventViewModel = eventViewModel,
                        token = userToken,
                        onBack = { dashboardScreen = null },
                        filterByRole = "organisateur"
                    )
                    showingDashboard && dashboardScreen == "events" -> EventsManagementScreen(
                        adminViewModel = adminViewModel,
                        eventViewModel = eventViewModel,
                        token = userToken,
                        onBack = { dashboardScreen = null },
                        onEventClick = { event ->
                            selectedEvent = event
                            showingDashboard = false
                            dashboardScreen = null
                        }
                    )
                    showingDashboard && dashboardScreen == "stats" -> StatsScreen(
                        adminViewModel = adminViewModel,
                        eventViewModel = eventViewModel,
                        token = userToken,
                        onBack = { dashboardScreen = null }
                    )
                    showingDashboard -> DashboardScreen(
                        adminViewModel = adminViewModel,
                        eventViewModel = eventViewModel,
                        token = userToken,
                        onLogout = {
                            isLoggedIn = false
                            isSignup = false
                            userToken = ""
                            userRole = ""
                            userName = ""
                            userEmail = ""
                            userId = ""
                            showingDashboard = false
                            dashboardScreen = null
                        },
                        onBack = {
                            showingDashboard = false
                            dashboardScreen = null
                        },
                        onNavigateToUsers = { dashboardScreen = "users" },
                        onNavigateToEvents = { dashboardScreen = "events" },
                        onNavigateToParticipants = { dashboardScreen = "participants" },
                        onNavigateToOrganisateurs = { dashboardScreen = "organisateurs" }
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
                        },
                        //onDashboardClick = { showingDashboard = true }
                    )
                }
            }
        }
    }
}
