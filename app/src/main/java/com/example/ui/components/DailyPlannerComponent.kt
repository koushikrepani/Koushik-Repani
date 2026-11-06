package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TaskEntity
import com.example.data.local.parseSubtasks
import com.example.ui.theme.PulseIndigoPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A comprehensive Daily Planner component in Jetpack Compose that lists tasks for the
 * current day with reactive checkboxes backed by Room persistent storage.
 */
@Composable
fun DailyPlannerComponent(
    todayTasks: List<TaskEntity>,
    onToggleTaskComplete: (TaskEntity) -> Unit,
    onTaskClick: (TaskEntity) -> Unit,
    onQuickAddTask: (title: String, category: String, priority: String) -> Unit,
    onStartFocus: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var quickTaskTitle by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("Medium") }
    var selectedFilterTab by remember { mutableIntStateOf(0) } // 0: All, 1: To Do, 2: Done
    val focusManager = LocalFocusManager.current

    val currentDateStr = remember {
        SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
    }
    val timeFormat = remember {
        SimpleDateFormat("h:mm a", Locale.getDefault())
    }

    val totalCount = todayTasks.size
    val completedCount = todayTasks.count { it.isCompleted }
    val pendingCount = totalCount - completedCount
    val completionFraction = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val animatedProgress by animateFloatAsState(targetValue = completionFraction, label = "dailyProgress")

    val displayedTasks = remember(todayTasks, selectedFilterTab) {
        when (selectedFilterTab) {
            1 -> todayTasks.filter { !it.isCompleted }
            2 -> todayTasks.filter { it.isCompleted }
            else -> todayTasks
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_planner_component_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = com.example.ui.theme.VoidSurface.copy(alpha = 0.88f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            com.example.ui.theme.GlassBorderBrush
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            // Header Section: Title, Date Chip & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(com.example.ui.theme.NeonIris.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EventNote,
                            contentDescription = null,
                            tint = com.example.ui.theme.NeonIris,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Daily Planner",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = currentDateStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (completedCount == totalCount && totalCount > 0) {
                        com.example.ui.theme.NeonMint.copy(alpha = 0.2f)
                    } else {
                        com.example.ui.theme.VoidSurfaceVariant
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (completedCount == totalCount && totalCount > 0) com.example.ui.theme.NeonMint.copy(alpha = 0.5f) else com.example.ui.theme.GlassBorder
                    )
                ) {
                    Text(
                        text = "$completedCount/$totalCount Done",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (completedCount == totalCount && totalCount > 0) com.example.ui.theme.NeonMint else Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Daily Progress Bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (completionFraction >= 1f) Color(0xFF10B981) else PulseIndigoPrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Filter Tabs (All / To Do / Done)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "All ($totalCount)" to 0,
                    "To-Do ($pendingCount)" to 1,
                    "Done ($completedCount)" to 2
                ).forEach { (label, idx) ->
                    val isSelected = selectedFilterTab == idx
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedFilterTab = idx }
                            .testTag("daily_filter_tab_$idx"),
                        color = if (isSelected) PulseIndigoPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, PulseIndigoPrimary) else null,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) PulseIndigoPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Inline Task Creator: Instant addition to Room
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = quickTaskTitle,
                    onValueChange = { quickTaskTitle = it },
                    placeholder = {
                        Text(
                            "Add a task for today...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (quickTaskTitle.isNotBlank()) {
                                onQuickAddTask(quickTaskTitle, "Work", selectedPriority)
                                quickTaskTitle = ""
                                focusManager.clearFocus()
                            }
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("daily_planner_quick_input")
                )

                // Priority switcher for quick task
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            selectedPriority = when (selectedPriority) {
                                "Medium" -> "High"
                                "High" -> "Urgent"
                                "Urgent" -> "Low"
                                else -> "Medium"
                            }
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    PriorityBadge(priority = selectedPriority)
                }

                IconButton(
                    onClick = {
                        if (quickTaskTitle.isNotBlank()) {
                            onQuickAddTask(quickTaskTitle, "Work", selectedPriority)
                            quickTaskTitle = ""
                            focusManager.clearFocus()
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (quickTaskTitle.isNotBlank()) PulseIndigoPrimary else MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("daily_planner_quick_add_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add task for today",
                        tint = if (quickTaskTitle.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tasks List with Checkboxes (Persistent via Room)
            if (displayedTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (completedCount == totalCount && totalCount > 0) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (completedCount == totalCount && totalCount > 0) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (completedCount == totalCount && totalCount > 0) {
                                "All today's tasks completed! Outstanding work! 🎉"
                            } else if (selectedFilterTab == 1) {
                                "No pending tasks for today!"
                            } else {
                                "No tasks scheduled for today. Type a goal above!"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    displayedTasks.forEach { task ->
                        val subtasks = parseSubtasks(task.subtasksRaw)
                        val dueTimeStr = timeFormat.format(Date(task.dueDate))

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onTaskClick(task) }
                                .testTag("daily_planner_task_${task.id}"),
                            color = if (task.isCompleted) {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                0.5.dp,
                                if (task.isCompleted) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Reactive Checkbox connected to Room
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = { onToggleTaskComplete(task) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = PulseIndigoPrimary,
                                        checkmarkColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("daily_task_checkbox_${task.id}")
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        PriorityBadge(priority = task.priority)
                                        Text(
                                            text = "• ${task.category}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Filled.Schedule,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = dueTimeStr,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                            )
                                        }

                                        if (subtasks.isNotEmpty()) {
                                            Text(
                                                text = "${subtasks.count { it.isDone }}/${subtasks.size} subtasks",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = PulseIndigoPrimary
                                            )
                                        }
                                    }
                                }

                                // Quick launch Focus timer for this specific task
                                IconButton(
                                    onClick = { onStartFocus(task) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(PulseIndigoPrimary.copy(alpha = 0.12f))
                                        .testTag("daily_task_focus_btn_${task.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = "Start Focus",
                                        tint = PulseIndigoPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
