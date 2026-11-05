package com.example.ui.screens.focus

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FocusSessionEntity
import com.example.ui.components.CategoryChip
import com.example.ui.components.MetricRing
import com.example.ui.theme.PulseIndigoPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FocusScreen(
    sessionType: String,
    totalSeconds: Int,
    remainingSeconds: Int,
    isRunning: Boolean,
    taskTitle: String,
    selectedTag: String,
    ambientSound: String,
    todayMinutes: Int,
    recentSessions: List<FocusSessionEntity>,
    onSelectMode: (type: String, minutes: Int) -> Unit,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onAdjustTime: (deltaSeconds: Int) -> Unit,
    onSelectTag: (String) -> Unit,
    onSelectSound: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (totalSeconds > 0) {
        ((totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val formattedTime = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    val soundOptions = listOf("None", "Rain", "White Noise", "Binaural", "Cafe")
    val tags = listOf("Deep Work", "Study", "Coding", "Writing", "Creative")

    val dateFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("focus_screen_content")
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "FOCUS STUDIO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Deep Work Session",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Mode Selector Tabs (Focus 25m, Short Break 5m, Long Break 15m)
        item {
            val modes = listOf("Focus" to 25, "Short Break" to 5, "Long Break" to 15)
            val selectedIndex = modes.indexOfFirst { it.first == sessionType }.coerceAtLeast(0)

            TabRow(
                selectedTabIndex = selectedIndex,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .testTag("focus_mode_tabs")
            ) {
                modes.forEachIndexed { index, (name, duration) ->
                    Tab(
                        selected = selectedIndex == index,
                        onClick = { onSelectMode(name, duration) },
                        text = {
                            Text(
                                text = "$name (${duration}m)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("focus_tab_$name")
                    )
                }
            }
        }

        // Active Task banner if linked
        if (taskTitle.isNotBlank()) {
            item {
                Surface(
                    color = PulseIndigoPrimary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PulseIndigoPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Filled.Timer, contentDescription = null, tint = PulseIndigoPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Linked Task Target", style = MaterialTheme.typography.labelSmall, color = PulseIndigoPrimary)
                            Text(taskTitle, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Big Circular Countdown Timer
        item {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .testTag("focus_timer_ring_box"),
                contentAlignment = Alignment.Center
            ) {
                MetricRing(
                    progress = progress,
                    size = 230.dp,
                    strokeWidth = 14.dp,
                    ringColor = if (sessionType == "Focus") PulseIndigoPrimary else Color(0xFF10B981),
                    ringGradient = if (sessionType == "Focus") {
                        listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFF06B6D4))
                    } else {
                        listOf(Color(0xFF10B981), Color(0xFF06B6D4))
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formattedTime,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("focus_countdown_text")
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = if (isRunning) "FLOW STATE ACTIVE" else "PAUSED",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = if (isRunning) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // Timer Controls: Adjust (-5m), Big Play/Pause, Reset, Adjust (+5m)
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedIconButton(
                    onClick = { onAdjustTime(-300) },
                    modifier = Modifier.size(46.dp).testTag("timer_minus_5m")
                ) {
                    Icon(imageVector = Icons.Filled.Remove, contentDescription = "Minus 5m")
                }

                FilledIconButton(
                    onClick = onToggleTimer,
                    modifier = Modifier
                        .size(72.dp)
                        .testTag("toggle_focus_timer_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isRunning) MaterialTheme.colorScheme.secondary else PulseIndigoPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start",
                        modifier = Modifier.size(36.dp)
                    )
                }

                OutlinedIconButton(
                    onClick = onResetTimer,
                    modifier = Modifier.size(46.dp).testTag("reset_focus_timer_button")
                ) {
                    Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Reset Timer")
                }

                OutlinedIconButton(
                    onClick = { onAdjustTime(300) },
                    modifier = Modifier.size(46.dp).testTag("timer_plus_5m")
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Plus 5m")
                }
            }
        }

        // Session Tag Selector
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Session Tag",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(tags) { tag ->
                        CategoryChip(
                            category = tag,
                            isSelected = selectedTag == tag,
                            onClick = { onSelectTag(tag) }
                        )
                    }
                }
            }
        }

        // Ambient Soundscapes
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("focus_ambient_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.GraphicEq,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ambient Focus Soundscapes",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(soundOptions) { sound ->
                            val isSelected = ambientSound == sound
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) PulseIndigoPrimary else MaterialTheme.colorScheme.surface,
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(
                                    0.5.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier
                                    .clickable { onSelectSound(sound) }
                                    .testTag("sound_option_$sound")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSelected && sound != "None") {
                                        Icon(
                                            imageVector = Icons.Filled.MusicNote,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = sound,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Today's Focus Log History
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Focus Log",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${todayMinutes}m logged",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (recentSessions.isEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No focus sessions recorded yet today.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                } else {
                    recentSessions.take(5).forEach { session ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp,
                            border = androidx.compose.foundation.BorderStroke(
                                0.5.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(PulseIndigoPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (session.taskTitle.isNotBlank()) session.taskTitle else session.tag,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${session.tag} • ${dateFormat.format(Date(session.completedAt))}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Text(
                                    text = "+${session.durationMinutes}m",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
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
