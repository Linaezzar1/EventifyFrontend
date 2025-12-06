import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eventify.app.model.Task
import com.eventify.app.model.User

@Composable
fun LogisticEventTaskItem(
    task: Task,
    currentUser: User,
    isLogisticManager: Boolean,
    onStatusChange: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isAssignedToMe = task.assignedTo?._id == currentUser._id
    val isOverdue = task.status == "en_retard"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3)
                )

                if (isOverdue) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "En retard",
                            tint = Color(0xFFF44336)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "En retard",
                            color = Color(0xFFF44336),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            task.description?.let {
                Text(text = it, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = "Assignée à : ${task.assignedTo?.name ?: "Non assignée"}",
                style = MaterialTheme.typography.bodySmall
            )

            task.dueDate?.let {
                Text(
                    text = "Échéance : $it",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isAssignedToMe || isLogisticManager) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Statut :", style = MaterialTheme.typography.bodySmall)

                    Row {
                        StatusChip(
                            label = "À faire",
                            selected = task.status == "a_faire",
                            onClick = { onStatusChange("a_faire") }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        StatusChip(
                            label = "En cours",
                            selected = task.status == "en_cours",
                            onClick = { onStatusChange("en_cours") }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        StatusChip(
                            label = "Terminée",
                            selected = task.status == "termine",
                            onClick = { onStatusChange("termine") }
                        )
                    }
                }
            } else {
                Text(
                    text = "Statut : ${task.status}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (isLogisticManager) {
                Spacer(modifier = Modifier.height(8.dp))
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onEdit) {
                        Text("Modifier")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFFF44336)
                        )
                    ) {
                        Text("Supprimer")
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) Color(0xFF2196F3) else Color.LightGray,
            labelColor = if (selected) Color.White else Color.DarkGray
        )
    )
}
