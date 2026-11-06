package com.example.ui.screens.today

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.local.DailyMoodEntity
import com.example.data.local.HabitEntity
import com.example.data.local.TaskEntity
import com.example.ui.components.DailyPlannerComponent
import com.example.ui.components.MetricRing
import com.example.ui.components.StreakFlameBadge
import com.example.ui.components.getHabitIcon
import com.example.ui.navigation.Screen
import com.example.ui.theme.AuroraBrush
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
import java.util.Date
import java.util.Locale

@Composable
fun TodayScreen(
    userName: String,
    tasks: List<TaskEntity>,
    todayTasks: List<TaskEntity>,
    habits: List<HabitEntity>,
    todayCompletedHabitIds: Set<Long>,
    todayFocusMinutes: Int,
    todayMood: DailyMoodEntity?,
    onNavigate: (Screen) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenMoodLog: () -> Unit,
    onToggleHabit: (HabitEntity) -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onTaskClick: (TaskEntity) -> Unit,
    onHabitClick: (HabitEntity) -> Unit,
    onNewTask: () -> Unit,
    onQuickAddTodayTask: (title: String, category: String, priority: String) -> Unit,
    onStartFocus: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Morning Focus"
            in 12..16 -> "Peak Momentum"
            in 17..21 -> "Evening Winddown"
            else -> "Night Sanctuary"
        }
    }

    val todayFormatted = remember {
        SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date())
    }

    val totalHabits = habits.size
    val completedHabitsCount = habits.count { todayCompletedHabitIds.contains(it.id) }
    val habitProgress = if (totalHabits > 0) completedHabitsCount.toFloat() / totalHabits else 0f

    val pendingTasks = tasks.filter { !it.isCompleted }
    val completedTasks = tasks.filter { it.isCompleted }
    val taskProgress = if (tasks.isNotEmpty()) completedTasks.size.toFloat() / tasks.size else 0f
    val overallScore = (((habitProgress + taskProgress) / 2f) * 100).toInt()

    // Ambient aurora background pulse
    val infiniteTransition = rememberInfiniteTransition(label = "auroraGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auroraPulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
            .drawBehind {
                // Top ambient aurora nebula
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonIris.copy(alpha = glowAlpha),
                            NeonCyan.copy(alpha = glowAlpha * 0.4f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.8f, size.height * 0.15f),
                        radius = size.width * 0.7f
                    )
                )
            }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("today_screen_content"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Greeting, Date Pill, Profile Settings
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
                                text = "⚡ $todayFormatted • $greeting",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Hello, $userName",
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(VoidSurfaceVariant)
                            .border(1.dp, GlassBorder, CircleShape)
                            .testTag("today_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                }
            }

            // Futuristic Hero Glass Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("today_hero_progress_card"),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = VoidSurface.copy(alpha = 0.88f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderBrush)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "DAILY FLOW RHYTHM",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = NeonAmber
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (overallScore >= 100) "Optimal State Achieved! 🏆" else "$overallScore% Trajectory",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$completedHabitsCount/$totalHabits habits • ${pendingTasks.size} tasks remaining",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            MetricRing(
                                progress = ((habitProgress + taskProgress) / 2f).coerceIn(0f, 1f),
                                size = 84.dp,
                                strokeWidth = 9.dp,
                                ringColor = NeonIris,
                                ringGradient = listOf(NeonIris, NeonCyan, NeonMint),
                                trackColor = Color(0x331E293B)
                            ) {
                                Text(
                                    text = "$overallScore%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // 3 Futuristic Stat Capsules
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = VoidSurfaceVariant.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, GlassBorder)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Habits Done", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color(0xFF94A3B8))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("$completedHabitsCount / $totalHabits", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NeonAmber)
                                }
                            }

                            Surface(
                                modifier = Modifier.weight(1f),
                                color = VoidSurfaceVariant.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, GlassBorder)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Tasks Done", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color(0xFF94A3B8))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("${completedTasks.size} / ${tasks.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NeonIris)
                                }
                            }

                            Surface(
                                modifier = Modifier.weight(1f),
                                color = VoidSurfaceVariant.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, GlassBorder)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Deep Focus", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color(0xFF94A3B8))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("${todayFocusMinutes}m", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NeonMint)
                                }
                            }
                        }
                    }
                }
            }

            // Quick Habits Carousel
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Habit Rhythms",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "View all →",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        modifier = Modifier
                            .clickable { onNavigate(Screen.Habits) }
                            .testTag("see_all_habits_link")
                    )
                }
            }

            if (habits.isNotEmpty()) {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(habits, key = { it.id }) { habit ->
                            val isDone = todayCompletedHabitIds.contains(habit.id)
                            val accentColor = remember(habit.colorHex) {
                                try {
                                    Color(android.graphics.Color.parseColor(habit.colorHex))
                                } catch (_: Exception) {
                                    NeonIris
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .width(165.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { onHabitClick(habit) }
                                    .testTag("today_habit_card_${habit.id}"),
                                color = VoidSurface,
                                shape = RoundedCornerShape(18.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isDone) accentColor.copy(alpha = 0.6f) else GlassBorder
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(accentColor.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = getHabitIcon(habit.iconName),
                                                contentDescription = null,
                                                tint = accentColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        StreakFlameBadge(streak = habit.currentStreak)
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = habit.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = Color.White
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = { onToggleHabit(habit) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(36.dp)
                                            .testTag("check_habit_btn_${habit.id}"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isDone) accentColor else VoidSurfaceVariant,
                                            contentColor = if (isDone) Color.White else Color(0xFF94A3B8)
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isDone) Icons.Filled.Check else Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isDone) "Done" else "Check in",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Daily Planner Component (Current day tasks with Room-backed checkboxes)
            item {
                val plannerTasks = if (todayTasks.isNotEmpty()) {
                    todayTasks
                } else {
                    tasks.filter {
                        val cal = Calendar.getInstance()
                        val taskCal = Calendar.getInstance().apply { timeInMillis = it.dueDate }
                        cal.get(Calendar.YEAR) == taskCal.get(Calendar.YEAR) &&
                        cal.get(Calendar.DAY_OF_YEAR) == taskCal.get(Calendar.DAY_OF_YEAR)
                    }.ifEmpty { tasks.take(5) }
                }

                DailyPlannerComponent(
                    todayTasks = plannerTasks,
                    onToggleTaskComplete = onToggleTask,
                    onTaskClick = onTaskClick,
                    onQuickAddTask = onQuickAddTodayTask,
                    onStartFocus = onStartFocus
                )
            }

            // Daily Reflection & Mood Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenMoodLog() }
                        .testTag("today_mood_reflection_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = VoidSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (todayMood != null) "Daily Mindset: ${todayMood.moodEmoji}" else "Mindset & Daily Reflection",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (todayMood != null && todayMood.reflectionNote.isNotBlank()) {
                                    "\"${todayMood.reflectionNote}\""
                                } else {
                                    "Tap to calibrate your mood, energy, and gratitude."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(NeonCyan.copy(alpha = 0.15f))
                                .border(1.dp, NeonCyan.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (todayMood != null) Icons.Filled.Edit else Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
