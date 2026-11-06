package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PulseIndigoPrimary
import kotlinx.coroutines.delay
import java.util.Locale

enum class PomodoroMode(val displayName: String, val defaultMinutes: Int) {
    FOCUS("Focus", 25),
    SHORT_BREAK("Short Break", 5),
    LONG_BREAK("Long Break", 15)
}

/**
 * A standalone, self-contained Pomodoro-style focus timer component in Jetpack Compose
 * utilizing pure Compose state management for countdown logic, start/pause/reset controls,
 * and an animated circular progress indicator.
 */
@Composable
fun PomodoroTimerComponent(
    initialMode: PomodoroMode = PomodoroMode.FOCUS,
    onSessionCompleted: ((mode: PomodoroMode, durationMinutes: Int) -> Unit)? = null,
    onTimerTick: ((remainingSeconds: Int, isRunning: Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // --- Compose State Management for Countdown Logic ---
    var selectedMode by rememberSaveable { mutableStateOf(initialMode.name) }
    var totalSeconds by rememberSaveable { mutableIntStateOf(initialMode.defaultMinutes * 60) }
    var remainingSeconds by rememberSaveable { mutableIntStateOf(initialMode.defaultMinutes * 60) }
    var isRunning by rememberSaveable { mutableStateOf(false) }

    val currentMode = remember(selectedMode) {
        PomodoroMode.entries.find { it.name == selectedMode } ?: PomodoroMode.FOCUS
    }

    // Countdown loop managed purely by Compose coroutine effect
    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
            onTimerTick?.invoke(remainingSeconds, true)
        } else if (isRunning && remainingSeconds <= 0) {
            isRunning = false
            val completedMinutes = totalSeconds / 60
            onSessionCompleted?.invoke(currentMode, completedMinutes)
            onTimerTick?.invoke(0, false)
            // Auto switch to next recommended mode
            if (currentMode == PomodoroMode.FOCUS) {
                selectedMode = PomodoroMode.SHORT_BREAK.name
                totalSeconds = PomodoroMode.SHORT_BREAK.defaultMinutes * 60
                remainingSeconds = PomodoroMode.SHORT_BREAK.defaultMinutes * 60
            } else {
                selectedMode = PomodoroMode.FOCUS.name
                totalSeconds = PomodoroMode.FOCUS.defaultMinutes * 60
                remainingSeconds = PomodoroMode.FOCUS.defaultMinutes * 60
            }
        }
    }

    // Derived formatting & progress calculations
    val progress by remember(totalSeconds, remainingSeconds) {
        derivedStateOf {
            if (totalSeconds > 0) {
                ((totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
            } else 0f
        }
    }

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val formattedTime = remember(minutes, seconds) {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    // Theme color based on active mode
    val modeAccentColor by animateColorAsState(
        targetValue = when (currentMode) {
            PomodoroMode.FOCUS -> PulseIndigoPrimary
            PomodoroMode.SHORT_BREAK -> Color(0xFF10B981) // Emerald
            PomodoroMode.LONG_BREAK -> Color(0xFF06B6D4) // Cyan
        },
        animationSpec = tween(durationMillis = 400),
        label = "modeAccentColor"
    )

    // Pulse animation when timer is actively running
    val infiniteTransition = rememberInfiniteTransition(label = "timerPulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pomodoro_timer_component_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode Presets Tabs (Focus, Short Break, Long Break)
            TabRow(
                selectedTabIndex = currentMode.ordinal,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .testTag("pomodoro_mode_tab_row")
            ) {
                PomodoroMode.entries.forEachIndexed { index, mode ->
                    val isSelected = currentMode == mode
                    Tab(
                        selected = isSelected,
                        onClick = {
                            isRunning = false
                            selectedMode = mode.name
                            totalSeconds = mode.defaultMinutes * 60
                            remainingSeconds = mode.defaultMinutes * 60
                            onTimerTick?.invoke(remainingSeconds, false)
                        },
                        text = {
                            Text(
                                text = "${mode.displayName} (${mode.defaultMinutes}m)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        modifier = Modifier.testTag("pomodoro_tab_${mode.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Circular Progress Indicator & Countdown Display
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .testTag("pomodoro_progress_ring_box"),
                contentAlignment = Alignment.Center
            ) {
                MetricRing(
                    progress = progress,
                    size = 210.dp,
                    strokeWidth = 12.dp,
                    ringColor = modeAccentColor,
                    ringGradient = when (currentMode) {
                        PomodoroMode.FOCUS -> listOf(PulseIndigoPrimary, Color(0xFF8B5CF6), Color(0xFF06B6D4))
                        PomodoroMode.SHORT_BREAK -> listOf(Color(0xFF10B981), Color(0xFF34D399), Color(0xFF059669))
                        PomodoroMode.LONG_BREAK -> listOf(Color(0xFF06B6D4), Color(0xFF38BDF8), Color(0xFF0284C7))
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = if (isRunning) Modifier.scale(pulseScale) else Modifier
                ) {
                    Text(
                        text = formattedTime,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("pomodoro_countdown_display")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = modeAccentColor.copy(alpha = 0.15f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isRunning) modeAccentColor else MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isRunning) "RUNNING" else "PAUSED",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = modeAccentColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Start / Pause / Reset Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.testTag("pomodoro_controls_row")
            ) {
                // Adjust Time (-1m)
                OutlinedIconButton(
                    onClick = {
                        val newTime = (remainingSeconds - 60).coerceAtLeast(60)
                        remainingSeconds = newTime
                        totalSeconds = maxOf(totalSeconds, newTime)
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("pomodoro_minus_1m")
                ) {
                    Icon(imageVector = Icons.Filled.Remove, contentDescription = "Minus 1 minute")
                }

                // Reset Button
                OutlinedIconButton(
                    onClick = {
                        isRunning = false
                        remainingSeconds = totalSeconds
                        onTimerTick?.invoke(remainingSeconds, false)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("pomodoro_reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Reset Timer",
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Primary Start / Pause Toggle Button
                FilledIconButton(
                    onClick = {
                        isRunning = !isRunning
                        onTimerTick?.invoke(remainingSeconds, isRunning)
                    },
                    modifier = Modifier
                        .size(68.dp)
                        .testTag("pomodoro_play_pause_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = modeAccentColor,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start",
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Adjust Time (+1m)
                OutlinedIconButton(
                    onClick = {
                        val newTime = remainingSeconds + 60
                        remainingSeconds = newTime
                        totalSeconds = maxOf(totalSeconds, newTime)
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("pomodoro_plus_1m")
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Plus 1 minute")
                }

                // Quick +5m preset jump
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val newTime = remainingSeconds + 300
                            remainingSeconds = newTime
                            totalSeconds = maxOf(totalSeconds, newTime)
                        }
                        .testTag("pomodoro_plus_5m")
                ) {
                    Text(
                        text = "+5m",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
