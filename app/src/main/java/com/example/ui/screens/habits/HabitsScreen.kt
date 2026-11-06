package com.example.ui.screens.habits

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.ui.components.AnimatedDailyHabitTracker
import com.example.ui.components.CategoryChip
import com.example.ui.components.HabitDayDot
import com.example.ui.components.StreakFlameBadge
import com.example.ui.components.getHabitIcon
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
    onToggleHabitForDate: (HabitEntity, String) -> Unit,
    onHabitClick: (HabitEntity) -> Unit,
    onNewHabit: () -> Unit,
    onQuickAddHabit: (title: String, category: String, colorHex: String, iconName: String) -> Unit,
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
            val dayLabel = sdfDay.format(cal.time).take(1)
            list.add(dateStr to dayLabel)
        }
        list
    }

    val habitDateMap = remember(habitLogs) {
        habitLogs.groupBy { it.habitId }.mapValues { entry ->
            entry.value.map { it.dateString }.toSet()
        }
    }

    // Ambient Aurora glow pulse
    val infiniteTransition = rememberInfiniteTransition(label = "habitsGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.40f,
        animationSpec = infiniteRepeatable(
            animation = tween(3800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "habitsPulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonAmber.copy(alpha = glowAlpha * 0.8f),
                            NeonIris.copy(alpha = glowAlpha * 0.4f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.85f, size.height * 0.12f),
                        radius = size.width * 0.7f
                    )
                )
            }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("habits_screen_content"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = NeonAmber.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonAmber.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "🔥 DAILY RHYTHMS",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.Bold,
                                color = NeonAmber,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Habits & Streaks",
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = onNewHabit,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonAmber,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.testTag("habits_add_habit_top_btn")
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Habit", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Animated Daily Habit Tracker (Week carousel, spring bounce check-ins, Room persistence)
            item {
                AnimatedDailyHabitTracker(
                    habits = habits,
                    habitLogs = habitLogs,
                    onToggleHabitForDate = onToggleHabitForDate,
                    onHabitClick = onHabitClick,
                    onQuickAddHabit = onQuickAddHabit
                )
            }

            // Streak Overview Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("habits_streak_overview_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = VoidSurface.copy(alpha = 0.88f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderBrush)
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
                                text = "Consistency Velocity",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Micro-habits compound into unstoppable daily power.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = VoidSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonAmber.copy(alpha = 0.4f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${maxStreak}d",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = NeonAmber
                                    )
                                    Text(
                                        text = "Top Streak",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = VoidSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonMint.copy(alpha = 0.4f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$totalCompletions",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = NeonMint
                                    )
                                    Text(
                                        text = "Check-ins",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = Color(0xFF94A3B8)
                                    )
                                }
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
                                text = "No routines in this category",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Create a new habit to start building positive streaks",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onNewHabit,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VoidSurfaceVariant,
                                    contentColor = Color.White
                                )
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
                            NeonIris
                        }
                    }
                    val completedDates = habitDateMap[habit.id] ?: emptySet()

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onHabitClick(habit) }
                            .testTag("habit_card_${habit.id}"),
                        color = if (isDoneToday) VoidSurface.copy(alpha = 0.65f) else VoidSurface,
                        shape = RoundedCornerShape(20.dp),
                        border = if (isDoneToday) {
                            androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
                        } else {
                            androidx.compose.foundation.BorderStroke(1.dp, GlassBorderBrush)
                        }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(accentColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getHabitIcon(habit.iconName),
                                            contentDescription = null,
                                            tint = accentColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = habit.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${habit.category} • ${habit.targetDaysPerWeek} days/week",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF94A3B8)
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
                                    color = Color(0xFF94A3B8),
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
                                        containerColor = if (isDoneToday) accentColor else VoidSurfaceVariant,
                                        contentColor = if (isDoneToday) Color.White else Color(0xFF94A3B8)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
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
        }
    }
}
