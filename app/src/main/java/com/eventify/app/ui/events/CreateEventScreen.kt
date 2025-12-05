package com.eventify.app.ui.events

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.eventify.app.model.EventRequest
import com.eventify.app.model.User
import com.eventify.app.network.RetrofitInstance
import com.eventify.app.viewmodel.EventViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    eventViewModel: EventViewModel,
    token: String,
    onEventCreated: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    
    // For "assignée à" field
    var logistiqueUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var selectedLogistiqueUser by remember { mutableStateOf<User?>(null) }
    var expandedDropdown by remember { mutableStateOf(false) }
    var loadingUsers by remember { mutableStateOf(false) }

    val context = LocalContext.current
    
    // Load logistique users on screen load
    LaunchedEffect(Unit) {
        loadingUsers = true
        try {
            val allUsers = RetrofitInstance.api.getAllUsers("Bearer $token")
            logistiqueUsers = allUsers.filter { it.role.lowercase() == "logistique" }
        } catch (e: Exception) {
            error = "Erreur lors du chargement des utilisateurs logistique: ${e.localizedMessage}"
        } finally {
            loadingUsers = false
        }
    }

    val dateTime: LocalDateTime? = if (selectedDate != null && selectedTime != null) {
        LocalDateTime.of(selectedDate, selectedTime)
    } else null

    // Format ISO string
    val dateIso: String? = dateTime?.atZone(ZoneId.systemDefault())
        ?.withZoneSameInstant(ZoneId.of("UTC"))
        ?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Créer un événement") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Titre") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Bouton Date/Heure
            Button(
                onClick = {
                    val calendar = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                            // Ouvre automatiquement TimePicker après
                            TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    selectedTime = LocalTime.of(hourOfDay, minute)
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                true
                            ).show()
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (dateTime == null)
                        "Choisir la date et l'heure"
                    else
                        "Sélectionné : ${dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}"
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Lieu") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // "Assignée à" dropdown field
            ExposedDropdownMenuBox(
                expanded = expandedDropdown,
                onExpandedChange = { expandedDropdown = !expandedDropdown }
            ) {
                OutlinedTextField(
                    value = selectedLogistiqueUser?.name ?: "",
                    onValueChange = { },
                    label = { Text("Assignée à") },
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    placeholder = { Text("Sélectionner un utilisateur logistique") }
                )
                ExposedDropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false }
                ) {
                    if (loadingUsers) {
                        DropdownMenuItem(
                            text = { Text("Chargement...") },
                            onClick = { }
                        )
                    } else if (logistiqueUsers.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Aucun utilisateur logistique disponible") },
                            onClick = { }
                        )
                    } else {
                        logistiqueUsers.forEach { user ->
                            DropdownMenuItem(
                                text = { Text("${user.name} (${user.email})") },
                                onClick = {
                                    selectedLogistiqueUser = user
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    loading = true
                    error = null
                    eventViewModel.createEvent(
                        token,
                        EventRequest(
                            title = title,
                            description = description,
                            date = dateIso ?: "",
                            location = location,
                            logisticManager = selectedLogistiqueUser?._id
                        ),
                        onSuccess = {
                            loading = false
                            onEventCreated()
                        },
                        onError = { err ->
                            loading = false
                            error = err
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading && dateIso != null,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
            ) {
                Text("Créer l'événement", color = Color.White)
            }
            error?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(it, color = Color.Red)
            }
            if (loading) {
                Spacer(modifier = Modifier.height(12.dp))
                CircularProgressIndicator(color = Color(0xFFFF9800))
            }
        }
    }
}
