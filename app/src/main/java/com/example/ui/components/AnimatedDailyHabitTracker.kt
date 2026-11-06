package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.ui.theme.FlameGold
import com.example.ui.theme.PulseIndigoPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CalendarDayItem(
    val dateString: String,
    val dayName: String,
    val dayNumber: String,
    val isToday: Boolean
)

/**
 * Animated Daily Habit Tracker component that allows users to seamlessly
 * track and mark daily habits as completed across multiple days with
 * lively animations, spring scale effects, and persistence backed by Room.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnimatedDailyHabitTracker(
    habits: List<HabitEntity>,
    habitLogs: List<HabitLogEntity>,
    onToggleHabitForDate: (HabitEntity, String) -> Unit,
    onHabitClick: (HabitEntity) -> Unit,
    onQuickAddHabit: (title: String, category: String, colorHex: String, iconName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val todayDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    var selectedDateStr by remember { mutableStateOf(todayDateStr) }
    var isQuickAddExpanded by remember { mutableStateOf(false) }

    // Quick add state
    var newTitle by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("Health") }
    var newColorHex by remember { mutableStateOf("#6366F1") }
    var newIconName by remember { mutableStateOf("water") }

    val daysOfWeek = remember {
        val list = mutableListOf<CalendarDayItem>()
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val sdfName = SimpleDateFormat("EEE", Locale.getDefault())
        val sdfNum = SimpleDateFormat("d", Locale.getDefault())

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -5)
        for (i in 0..6) {
            val dateStr = sdfDate.format(cal.time)
            list.add(
                CalendarDayItem(
                    dateString = dateStr,
                    dayName = sdfName.format(cal.time).uppercase(),
                    dayNumber = sdfNum.format(cal.time),
                    isToday = dateStr == todayDateStr
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    // Set of completed habit IDs for the selected date
    val completedHabitIdsOnSelectedDate = remember(habitLogs, selectedDateStr) {
        habitLogs.filter { it.dateString == selectedDateStr }.map { it.habitId }.toSet()
    }

    val totalHabits = habits.size
    val completedCount = habits.count { completedHabitIdsOnSelectedDate.contains(it.id) }
    val progressFraction = if (totalHabits > 0) completedCount.toFloat() / totalHabits else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "masterHabitProgress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("animated_daily_habit_tracker_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            // Header: Title & Quick Add Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFF97316), Color(0xFFFF9800))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ElectricBolt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Habit Mastery & Streaks",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (selectedDateStr == todayDateStr) "Tracking: Today" else "Tracking: $selectedDateStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = { isQuickAddExpanded = !isQuickAddExpanded },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("toggle_quick_habit_add")
                ) {
                    Icon(
                        imageVector = if (isQuickAddExpanded) Icons.Filled.Close else Icons.Filled.Add,
                        contentDescription = "Quick add habit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Day Selector Carousel
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("habit_day_selector_carousel"),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                items(daysOfWeek) { dayItem ->
                    val isSelected = dayItem.dateString == selectedDateStr
                    val dayLogsCount = habitLogs.count { it.dateString == dayItem.dateString }

                    val capsuleBg by animateColorAsState(
                        targetValue = when {
                            isSelected -> PulseIndigoPrimary
                            dayItem.isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        },
                        animationSpec = tween(300),
                        label = "dayBgColor"
                    )

                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        label = "dayContentColor"
                    )

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedDateStr = dayItem.dateString }
                            .padding(horizontal = 3.dp)
                            .testTag("day_pill_${dayItem.dateString}"),
                        shape = RoundedCornerShape(16.dp),
                        color = capsuleBg,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = dayItem.dayName,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = dayItem.dayNumber,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = contentColor
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Mini check dot if at least one habit completed on that day
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            dayLogsCount > 0 && isSelected -> Color.White
                                            dayLogsCount > 0 -> Color(0xFF10B981)
                                            else -> Color.Transparent
                                        }
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Mastery Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when {
                                progressFraction >= 1f && totalHabits > 0 -> "🏆 Phenomenal! All daily habits crushed!"
                                progressFraction >= 0.5f -> "🔥 Peak momentum! Halfway through."
                                else -> "🌱 Small wins compound. Check in below!"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (progressFraction >= 1f && totalHabits > 0) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$completedCount/$totalHabits (${(animatedProgress * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PulseIndigoPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (progressFraction >= 1f && totalHabits > 0) Color(0xFF10B981) else Color(0xFFF97316),
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }
            }

            // Quick Add Form (Expandable)
            AnimatedVisibility(
                visible = isQuickAddExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Quick Add Routine",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        placeholder = { Text("e.g. 10m Morning Breathing Flow") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quick_habit_title_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Health", "Mind", "Fitness", "Wellness").forEach { cat ->
                            CategoryChip(
                                category = cat,
                                isSelected = newCategory == cat,
                                onClick = { newCategory = cat }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Color row & Submit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("#6366F1", "#06B6D4", "#F97316", "#10B981", "#F43F5E").forEach { hex ->
                                val color = Color(android.graphics.Color.parseColor(hex))
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable { newColorHex = hex }
                                        .then(
                                            if (newColorHex == hex) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                            else Modifier
                                        )
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (newTitle.isNotBlank()) {
                                    onQuickAddHabit(newTitle, newCategory, newColorHex, newIconName)
                                    newTitle = ""
                                    isQuickAddExpanded = false
                                }
                            },
                            enabled = newTitle.isNotBlank(),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("submit_quick_habit_btn")
                        ) {
                            Text("Create")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Habit Items List for the Selected Day
            if (habits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No active routines yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap + above to start your daily rhythm!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    habits.forEach { habit ->
                        val isDone = completedHabitIdsOnSelectedDate.contains(habit.id)
                        val accentColor = remember(habit.colorHex) {
                            try {
                                Color(android.graphics.Color.parseColor(habit.colorHex))
                            } catch (_: Exception) {
                                Color(0xFF6366F1)
                            }
                        }

                        AnimatedHabitCard(
                            habit = habit,
                            isDone = isDone,
                            accentColor = accentColor,
                            onToggle = { onToggleHabitForDate(habit, selectedDateStr) },
                            onClick = { onHabitClick(habit) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedHabitCard(
    habit: HabitEntity,
    isDone: Boolean,
    accentColor: Color,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    // Spring bounce scale on completion toggle
    val buttonScale by animateFloatAsState(
        targetValue = if (isDone) 1.0f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "btnScale"
    )

    // Pulsing flame animation for active streaks
    val infiniteTransition = rememberInfiniteTransition(label = "flameTransition")
    val flamePulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flamePulse"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("animated_habit_card_${habit.id}"),
        color = if (isDone) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
        },
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDone) accentColor.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Habit Icon with vibrant gradient backdrop
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getHabitIcon(habit.iconName),
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = habit.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = habit.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Animated Streak Flame
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = if (isDone) Modifier.scale(flamePulse) else Modifier
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = if (isDone) FlameGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${habit.currentStreak}d",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDone) FlameGold else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Interactive Spring Bouncy Check Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .scale(buttonScale)
                    .clip(CircleShape)
                    .background(
                        if (isDone) accentColor else MaterialTheme.colorScheme.surface
                    )
                    .border(
                        width = if (isDone) 0.dp else 1.5.dp,
                        color = if (isDone) Color.Transparent else accentColor.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggle
                    )
                    .testTag("habit_toggle_check_${habit.id}"),
                contentAlignment = Alignment.Center
            ) {
                // Expanding ripple ring canvas effect when checked
                if (isDone) {
                    Canvas(modifier = Modifier.size(44.dp)) {
                        drawCircle(
                            color = accentColor.copy(alpha = 0.25f),
                            radius = size.minDimension / 1.7f,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = if (isDone) "Completed" else "Mark complete",
                    tint = if (isDone) Color.White else accentColor.copy(alpha = 0.3f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
