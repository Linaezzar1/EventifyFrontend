package com.eventify.app.ui.tasks

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.eventify.app.model.Task
import com.eventify.app.model.User
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TaskEditDialog(
    task: Task?,
    participants: List<User>,
    onDismiss: () -> Unit,
    onSave: (String, String?, String, String?, Long?) -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description) }
    var status by remember { mutableStateOf(task?.status ?: "en attente") }
    var assignedTo by remember { mutableStateOf(task?.assignedTo?._id) }

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    var dueDate by remember { mutableStateOf(task?.dueDate?.let { parseToMillis(it) }) }
    dueDate?.let { calendar.timeInMillis = it }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                dueDate = calendar.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (task == null) "Créer une tâche" else "Modifier la tâche") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description ?: "",
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenuSelector(
                    label = "Statut",
                    options = listOf("en attente", "en cours", "fait"),
                    selected = status,
                    onSelectedChange = { status = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Dropdown assignation utilisateur
                var expandedUser by remember { mutableStateOf(false) }
                OutlinedButton(onClick = { expandedUser = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(assignedTo?.let { id ->
                        participants.find { it._id == id }?.let { "${it.name} (${it.role})" } ?: "Assignée à"
                    } ?: "Assignée à")
                }
                DropdownMenu(expanded = expandedUser, onDismissRequest = { expandedUser = false }) {
                    participants.forEach { user ->
                        DropdownMenuItem(
                            text = { Text("${user.name} (${user.role})") },
                            onClick = {
                                assignedTo = user._id
                                expandedUser = false
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = { datePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
                    Text(dueDate?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it)) } ?: "Sélectionner une date")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(title, description, status, assignedTo, dueDate)
            }) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
fun DropdownMenuSelector(
    label: String,
    options: List<String>,
    selected: String,
    onSelectedChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$label : $selected")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelectedChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun parseToMillis(dateString: String): Long? {
    return try {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).parse(dateString)?.time
    } catch (e: Exception) {
        null
    }
}
