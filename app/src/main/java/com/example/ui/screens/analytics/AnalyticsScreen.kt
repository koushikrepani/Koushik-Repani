package com.example.ui.screens.analytics

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DailyMoodEntity
import com.example.data.local.FocusSessionEntity
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.data.local.TaskEntity
import com.example.ui.components.MetricRing
import com.example.ui.components.StreakFlameBadge
import com.example.ui.theme.FlameGold
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderBrush
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonIris
import com.example.ui.theme.NeonMint
import com.example.ui.theme.VoidBlack
import com.example.ui.theme.VoidSurface
import com.example.ui.theme.VoidSurfaceVariant

@Composable
fun AnalyticsScreen(
    tasks: List<TaskEntity>,
    habits: List<HabitEntity>,
    habitLogs: List<HabitLogEntity>,
    focusSessions: List<FocusSessionEntity>,
    moods: List<DailyMoodEntity>,
    modifier: Modifier = Modifier
) {
    val completedTasksCount = tasks.count { it.isCompleted }
    val totalTasksCount = tasks.size
    val taskRate = if (totalTasksCount > 0) completedTasksCount.toFloat() / totalTasksCount else 0.5f

    val totalHabitsCount = habits.size
    val maxStreak = habits.maxOfOrNull { it.currentStreak } ?: 0
    val totalFocusMinutes = focusSessions.sumOf { it.durationMinutes }

    // Overall Productivity Score (0-100)
    val productivityScore = remember(taskRate, habits, totalFocusMinutes) {
        val habitFactor = (habits.sumOf { it.currentStreak } * 3).coerceAtMost(35)
        val taskFactor = (taskRate * 35).toInt()
        val focusFactor = (totalFocusMinutes / 5).coerceAtMost(30)
        (habitFactor + taskFactor + focusFactor).coerceIn(20, 100)
    }

    // Focus distribution by Tag
    val tagDistribution = remember(focusSessions) {
        focusSessions.groupBy { it.tag }.mapValues { entry ->
            entry.value.sumOf { it.durationMinutes }
        }
    }

    // Ambient Aurora glow pulse
    val infiniteTransition = rememberInfiniteTransition(label = "analyticsGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.38f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "analyticsPulse"
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
                            NeonCyan.copy(alpha = glowAlpha * 0.4f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.8f, size.height * 0.15f),
                        radius = size.width * 0.75f
                    )
                )
            }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("analytics_screen_content"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Column {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = NeonCyan.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "📊 BIOMETRIC & PERFORMANCE INTEL",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Productivity Insights",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            // Productivity Velocity Hero Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("analytics_velocity_card"),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = VoidSurface.copy(alpha = 0.88f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderBrush)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Flow Trajectory Score",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when {
                                    productivityScore >= 80 -> "Elite flow! Peak momentum sustained across habits & focus."
                                    productivityScore >= 50 -> "Solid rhythm! Daily tasks on track with positive trajectory."
                                    else -> "Building traction. Complete tasks & log focus sessions to spike score!"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        MetricRing(
                            progress = productivityScore / 100f,
                            size = 86.dp,
                            strokeWidth = 10.dp,
                            ringColor = NeonIris,
                            ringGradient = listOf(NeonIris, NeonCyan, NeonMint),
                            trackColor = Color(0x331E293B)
                        ) {
                            Text(
                                text = "$productivityScore",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Summary Metric Capsules (3 columns)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = VoidSurface,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Completion", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${(taskRate * 100).toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NeonIris)
                            Text("$completedTasksCount of $totalTasksCount", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color(0xFF94A3B8))
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        color = VoidSurface,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Peak Streak", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${maxStreak}d", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NeonAmber)
                            Text("$totalHabitsCount habits active", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color(0xFF94A3B8))
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        color = VoidSurface,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Total Focus", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${totalFocusMinutes}m", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NeonMint)
                            Text("${focusSessions.size} sprints logged", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color(0xFF94A3B8))
                        }
                    }
                }
            }

            // Habit Consistency Bars
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("analytics_habits_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = VoidSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Habit Consistency Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        if (habits.isEmpty()) {
                            Text("No habits tracked yet.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                        } else {
                            habits.forEach { habit ->
                                val progressFrac = (habit.currentStreak.toFloat() / 14f).coerceIn(0.1f, 1f)
                                val accentColor = remember(habit.colorHex) {
                                    try {
                                        Color(android.graphics.Color.parseColor(habit.colorHex))
                                    } catch (_: Exception) {
                                        NeonIris
                                    }
                                }

                                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = habit.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                        StreakFlameBadge(streak = habit.currentStreak)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    LinearProgressIndicator(
                                        progress = { progressFrac },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = accentColor,
                                        trackColor = VoidSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Focus Tag Breakdown
            if (tagDistribution.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = VoidSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Focus Time by Domain",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            tagDistribution.forEach { (tag, minutes) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = tag, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = NeonMint.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "${minutes}m",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonMint,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Milestone & Achievement Badges
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("analytics_achievements_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = VoidSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.EmojiEvents, contentDescription = null, tint = FlameGold)
                            Text(
                                text = "Milestones & Achievements",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        listOf(
                            Triple("🚀 Day 1 Trailblazer", "Configured daily routines and initial agenda", true),
                            Triple("🔥 Flame Keeper", "Maintained active 3+ day streak", maxStreak >= 3),
                            Triple("⏱️ Deep Work Vanguard", "Completed 50+ total focus minutes", totalFocusMinutes >= 50),
                            Triple("✅ Task Crusher", "Completed 3+ tasks across projects", completedTasksCount >= 3)
                        ).forEach { (title, subtitle, unlocked) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (unlocked) FlameGold.copy(alpha = 0.2f)
                                            else VoidSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                        .border(
                                            1.dp,
                                            if (unlocked) FlameGold.copy(alpha = 0.5f) else Color.Transparent,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = if (unlocked) FlameGold else Color(0xFF64748B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (unlocked) Color.White else Color(0xFF64748B)
                                    )
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8)
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
