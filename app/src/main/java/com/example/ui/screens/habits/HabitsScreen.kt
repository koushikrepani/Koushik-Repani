package com.example.ui.screens.habits

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.ui.components.CategoryChip
import com.example.ui.components.HabitDayDot
import com.example.ui.components.StreakFlameBadge
import com.example.ui.components.getHabitIcon
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HabitsScreen(
    habits: List<HabitEntity>,
    habitLogs: List<HabitLogEntity>,
    todayCompletedHabitIds: Set<Long>,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    onToggleHabitToday: (HabitEntity) -> Unit,
    onHabitClick: (HabitEntity) -> Unit,
    onNewHabit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("All", "Health", "Mind", "Fitness", "Wellness", "Productivity")

    val filteredHabits = remember(habits, selectedCategory) {
        if (selectedCategory == "All") habits
        else habits.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    val totalCompletions = habits.sumOf { it.totalCompletions }
    val maxStreak = habits.maxOfOrNull { it.currentStreak } ?: 0

    // Past 7 days definitions
    val past7Days = remember {
        val list = mutableListOf<Pair<String, String>>()
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val sdfDay = SimpleDateFormat("EEE", Locale.US)
        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = sdfDate.format(cal.time)
            val dayLabel = sdfDay.format(cal.time).take(1) // M, T, W...
            list.add(dateStr to dayLabel)
        }
        list
    }

    // Build map of habitId to set of completed dates
    val habitDateMap = remember(habitLogs) {
        habitLogs.groupBy { it.habitId }.mapValues { entry ->
            entry.value.map { it.dateString }.toSet()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("habits_screen_content")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HABITS & RHYTHMS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Daily Streaks",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onNewHabit,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("habits_add_habit_top_btn")
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Habit")
                }
            }
        }

        // Streak Overview Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("habits_streak_overview_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Consistency Score",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Small daily wins compound into extraordinary results.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${maxStreak}d",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Top Streak",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$totalCompletions",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Check-ins",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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

        if (filteredHabits.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No routines in this category",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Create a new habit to start building positive streaks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onNewHabit,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Create Habit")
                        }
                    }
                }
            }
        } else {
            items(filteredHabits, key = { it.id }) { habit ->
                val isDoneToday = todayCompletedHabitIds.contains(habit.id)
                val accentColor = remember(habit.colorHex) {
                    try {
                        Color(android.graphics.Color.parseColor(habit.colorHex))
                    } catch (_: Exception) {
                        Color(0xFF6366F1)
                    }
                }
                val completedDates = habitDateMap[habit.id] ?: emptySet()

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onHabitClick(habit) }
                        .testTag("habit_card_${habit.id}"),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDoneToday) accentColor.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    ),
                    tonalElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(accentColor.copy(alpha = 0.18f)),
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
                                    Text(
                                        text = habit.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${habit.category} • ${habit.targetDaysPerWeek} days/week",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            StreakFlameBadge(streak = habit.currentStreak)
                        }

                        if (habit.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = habit.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 7-day past week progress row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                past7Days.forEach { (dateStr, dayLabel) ->
                                    val isDone = completedDates.contains(dateStr)
                                    HabitDayDot(
                                        isDone = isDone,
                                        dayLabel = dayLabel,
                                        accentColor = accentColor
                                    )
                                }
                            }

                            Button(
                                onClick = { onToggleHabitToday(habit) },
                                modifier = Modifier
                                    .height(40.dp)
                                    .testTag("toggle_habit_btn_${habit.id}"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDoneToday) accentColor else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isDoneToday) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isDoneToday) Icons.Filled.Check else Icons.Filled.RadioButtonUnchecked,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isDoneToday) "Done" else "Check in", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}
