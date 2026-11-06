package com.example.ui.screens.planner

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TaskEntity
import com.example.data.local.parseSubtasks
import com.example.ui.components.CategoryChip
import com.example.ui.components.PriorityBadge
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderBrush
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonIris
import com.example.ui.theme.NeonMint
import com.example.ui.theme.VoidBlack
import com.example.ui.theme.VoidSurface
import com.example.ui.theme.VoidSurfaceVariant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PlannerScreen(
    tasks: List<TaskEntity>,
    searchQuery: String,
    selectedCategory: String,
    selectedPriority: String,
    selectedTab: Int,
    onSearchChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onPriorityChange: (String) -> Unit,
    onTabChange: (Int) -> Unit,
    onToggleTaskComplete: (TaskEntity) -> Unit,
    onTaskClick: (TaskEntity) -> Unit,
    onStartFocus: (TaskEntity) -> Unit,
    onNewTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("All", "Work", "Personal", "Health", "Study", "Finance")
    val priorities = listOf("All", "Urgent", "High", "Medium", "Low")

    val filteredTasks = remember(tasks, searchQuery, selectedCategory, selectedPriority, selectedTab) {
        tasks.filter { task ->
            val matchesTab = when (selectedTab) {
                1 -> !task.isCompleted
                2 -> task.isCompleted
                else -> true
            }
            val matchesCategory = selectedCategory == "All" || task.category.equals(selectedCategory, ignoreCase = true)
            val matchesPriority = selectedPriority == "All" || task.priority.equals(selectedPriority, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    task.title.contains(searchQuery, ignoreCase = true) ||
                    task.description.contains(searchQuery, ignoreCase = true)

            matchesTab && matchesCategory && matchesPriority && matchesSearch
        }
    }

    val pendingCount = tasks.count { !it.isCompleted }
    val completedCount = tasks.count { it.isCompleted }
    val completionPercent = if (tasks.isNotEmpty()) ((completedCount.toFloat() / tasks.size) * 100).toInt() else 0

    val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

    // Ambient Aurora glow pulse
    val infiniteTransition = rememberInfiniteTransition(label = "plannerGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.38f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "plannerPulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonIris.copy(alpha = glowAlpha),
                            NeonCyan.copy(alpha = glowAlpha * 0.35f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.15f, size.height * 0.12f),
                        radius = size.width * 0.75f
                    )
                )
            }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("planner_screen_content"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Title, Category pill, Add Task Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = NeonIris.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonIris.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "✦ MASTER AGENDA",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Planner & Tasks",
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = onNewTask,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonIris,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("planner_add_task_top_btn")
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Task", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Glassmorphic Metric Capsule Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("planner_stats_capsule"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = VoidSurface.copy(alpha = 0.88f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderBrush)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Workflow Velocity",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$completedCount completed • $pendingCount remaining",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = VoidSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonMint.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(NeonMint)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$completionPercent% Done",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonMint
                                )
                            }
                        }
                    }
                }
            }

            // Search Bar with glassmorphism
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = {
                        Text(
                            "Search tasks, subtasks, tags...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B)
                        )
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Search, contentDescription = "Search", tint = NeonIris)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear", tint = Color.White)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = VoidSurface,
                        unfocusedContainerColor = VoidSurface.copy(alpha = 0.7f),
                        focusedBorderColor = NeonIris,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("planner_search_input")
                )
            }

            // Status Tabs (All / Pending / Done)
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = VoidSurfaceVariant.copy(alpha = 0.7f),
                    contentColor = NeonIris,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
                        .testTag("planner_status_tabs")
                ) {
                    listOf(
                        "All (${tasks.size})" to 0,
                        "Pending ($pendingCount)" to 1,
                        "Done ($completedCount)" to 2
                    ).forEach { (label, idx) ->
                        val isSelected = selectedTab == idx
                        Tab(
                            selected = isSelected,
                            onClick = { onTabChange(idx) },
                            text = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8)
                                )
                            },
                            modifier = Modifier.testTag("planner_tab_$idx")
                        )
                    }
                }
            }

            // Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        CategoryChip(
                            category = cat,
                            isSelected = selectedCategory == cat,
                            onClick = { onCategoryChange(cat) }
                        )
                    }
                }
            }

            // Priority Filter Chips
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Priority:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                    priorities.forEach { prio ->
                        val isSelected = selectedPriority == prio
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onPriorityChange(prio) }
                                .testTag("filter_priority_$prio"),
                            color = if (isSelected) NeonIris.copy(alpha = 0.25f) else VoidSurfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, NeonIris) else androidx.compose.foundation.BorderStroke(0.5.dp, GlassBorder),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = prio,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NeonCyan else Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Task Items List
            if (filteredTasks.isEmpty()) {
                item {
                    Surface(
                        color = VoidSurface.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(22.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No matching tasks found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try clearing filters or add a new task",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    onSearchChange("")
                                    onCategoryChange("All")
                                    onPriorityChange("All")
                                    onTabChange(0)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VoidSurfaceVariant,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Reset Filters")
                            }
                        }
                    }
                }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    val subtasks = parseSubtasks(task.subtasksRaw)
                    val dueDateStr = dateFormat.format(Date(task.dueDate))
                    val isOverdue = task.dueDate < System.currentTimeMillis() && !task.isCompleted

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onTaskClick(task) }
                            .testTag("planner_task_item_${task.id}"),
                        color = if (task.isCompleted) VoidSurface.copy(alpha = 0.55f) else VoidSurface,
                        shape = RoundedCornerShape(18.dp),
                        border = if (isOverdue) {
                            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f))
                        } else if (task.isCompleted) {
                            androidx.compose.foundation.BorderStroke(1.dp, GlassBorder.copy(alpha = 0.5f))
                        } else {
                            androidx.compose.foundation.BorderStroke(1.dp, GlassBorderBrush)
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = task.isCompleted,
                                onCheckedChange = { onToggleTaskComplete(task) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = NeonIris,
                                    uncheckedColor = Color(0xFF64748B),
                                    checkmarkColor = Color.White
                                ),
                                modifier = Modifier.testTag("planner_task_checkbox_${task.id}")
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    PriorityBadge(priority = task.priority)
                                    Text(
                                        text = "• ${task.category}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                                    color = if (task.isCompleted) Color(0xFF64748B) else Color.White
                                )

                                if (task.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = task.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF94A3B8),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.CalendarMonth,
                                            contentDescription = null,
                                            tint = if (isOverdue) Color(0xFFEF4444) else Color(0xFF94A3B8),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = dueDateStr,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                            color = if (isOverdue) Color(0xFFEF4444) else Color(0xFF94A3B8)
                                        )
                                    }

                                    if (subtasks.isNotEmpty()) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = NeonIris.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "${subtasks.count { it.isDone }}/${subtasks.size} subtasks",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = NeonCyan,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            IconButton(
                                onClick = { onStartFocus(task) },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(NeonIris.copy(alpha = 0.18f))
                                    .border(1.dp, NeonIris.copy(alpha = 0.35f), CircleShape)
                                    .testTag("planner_focus_btn_${task.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Focus",
                                    tint = NeonIris,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
