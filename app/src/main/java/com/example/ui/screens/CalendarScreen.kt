package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalendarTask
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCobalt
import com.example.ui.theme.JarvisCrimson
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.viewmodel.JarvisViewModel

@Composable
fun CalendarScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val pendingCount = tasks.count { !it.isCompleted }

    Box(modifier = modifier.fillMaxSize().background(JarvisBackground)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(10.dp)) }

            // Agenda Header with Voice Briefing button
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ENCRYPTED CALENDAR",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JarvisCyan,
                                    letterSpacing = 1.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "$pendingCount Scheduled Tasks",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JarvisTextPrimary
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.submitQuery("Brief me on my agenda")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JarvisCyan,
                                    contentColor = JarvisBackground
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("voice_briefing_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Read Brief",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = JarvisEmerald,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Synced with Gemini Live Cloud Engine",
                                fontSize = 10.sp,
                                color = JarvisTextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Tasks List
            items(tasks, key = { it.id }) { task ->
                TaskItemCard(
                    task = task,
                    onToggleComplete = { viewModel.toggleTaskComplete(task.id) },
                    onDelete = { viewModel.deleteTask(task.id) }
                )
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }

        // Floating Action Button to Add Task
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = JarvisCyan,
            contentColor = JarvisBackground,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
                .testTag("add_task_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Calendar Task")
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onAddTask = { title, timeSlot, priority, category, desc ->
                viewModel.addTask(title, timeSlot, priority, category, desc)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun TaskItemCard(
    task: CalendarTask,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_item_${task.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) JarvisSurface.copy(alpha = 0.6f) else JarvisSurfaceElevated
        ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (task.isCompleted) JarvisBorder else JarvisBorder.copy(alpha = 0.8f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            IconButton(
                onClick = onToggleComplete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (task.isCompleted) "Completed" else "Pending",
                    tint = if (task.isCompleted) JarvisEmerald else JarvisTextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.isCompleted) JarvisTextMuted else JarvisTextPrimary,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        fontSize = 12.sp,
                        color = JarvisTextSecondary,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Time Slot
                    Text(
                        text = task.timeSlot,
                        fontSize = 10.sp,
                        color = JarvisCyanLight,
                        fontFamily = FontFamily.Monospace
                    )

                    // Priority Badge
                    val priorityColor = when (task.priority) {
                        TaskPriority.HIGH -> JarvisCrimson
                        TaskPriority.MEDIUM -> JarvisAmber
                        TaskPriority.LOW -> JarvisEmerald
                    }
                    Box(
                        modifier = Modifier
                            .border(0.6.dp, priorityColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .background(priorityColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.priority.label,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Category Badge
                    Box(
                        modifier = Modifier
                            .background(JarvisSurfaceVariant, RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.category.label,
                            fontSize = 9.sp,
                            color = JarvisTextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Delete action
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Task",
                    tint = JarvisTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAddTask: (String, String, TaskPriority, TaskCategory, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var timeSlot by remember { mutableStateOf("10:00 AM - 11:00 AM") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var category by remember { mutableStateOf(TaskCategory.WORK) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Encrypted Task",
                fontWeight = FontWeight.Bold,
                color = JarvisCyan,
                fontFamily = FontFamily.Monospace
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary,
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisBorder
                    )
                )

                OutlinedTextField(
                    value = timeSlot,
                    onValueChange = { timeSlot = it },
                    label = { Text("Time Slot") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary,
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisBorder
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Encrypted in Vault)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary,
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisBorder
                    )
                )

                // Priority selector
                Text("Priority", fontSize = 12.sp, color = JarvisTextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TaskPriority.values().forEach { prio ->
                        val isSel = prio == priority
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) JarvisCyan else JarvisSurfaceVariant)
                                .clickable { priority = prio }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = prio.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) JarvisBackground else JarvisTextPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAddTask(title, timeSlot, priority, category, description)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = JarvisBackground)
            ) {
                Text("Encrypt & Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = JarvisTextSecondary)
            }
        },
        containerColor = JarvisSurfaceElevated,
        shape = RoundedCornerShape(16.dp)
    )
}
