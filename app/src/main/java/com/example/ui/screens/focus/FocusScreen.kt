package com.example.ui.screens.focus

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FocusSessionEntity
import com.example.ui.components.CategoryChip
import com.example.ui.components.MetricRing
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderBrush
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

    // Ambient Aurora glow pulse
    val infiniteTransition = rememberInfiniteTransition(label = "focusGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "focusPulse"
    )

    val ringScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringPulse"
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
                            NeonMint.copy(alpha = glowAlpha * 0.4f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.30f),
                        radius = size.width * 0.8f
                    )
                )
            }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("focus_screen_content"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 140.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = NeonMint.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonMint.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "⚡ DEEP WORK CHAMBER",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.Bold,
                            color = NeonMint,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Focus Studio",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            // Mode Selector Tabs (Focus 25m, Short Break 5m, Long Break 15m)
            item {
                val modes = listOf("Focus" to 25, "Short Break" to 5, "Long Break" to 15)
                val selectedIndex = modes.indexOfFirst { it.first == sessionType }.coerceAtLeast(0)

                TabRow(
                    selectedTabIndex = selectedIndex,
                    containerColor = VoidSurfaceVariant.copy(alpha = 0.8f),
                    contentColor = NeonIris,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                        .testTag("focus_mode_tabs")
                ) {
                    modes.forEachIndexed { index, (name, duration) ->
                        val isSelected = selectedIndex == index
                        Tab(
                            selected = isSelected,
                            onClick = { onSelectMode(name, duration) },
                            text = {
                                Text(
                                    text = "$name (${duration}m)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8)
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
                        color = VoidSurface,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonIris.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Filled.Timer, contentDescription = null, tint = NeonCyan)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Linked Target Task", style = MaterialTheme.typography.labelSmall, color = NeonCyan)
                                Text(taskTitle, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Circular Countdown Timer Hero
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("focus_timer_card"),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = VoidSurface.copy(alpha = 0.88f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderBrush)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .then(if (isRunning) Modifier.scale(ringScale) else Modifier)
                                .testTag("focus_timer_ring_box"),
                            contentAlignment = Alignment.Center
                        ) {
                            MetricRing(
                                progress = progress,
                                size = 230.dp,
                                strokeWidth = 14.dp,
                                ringColor = if (sessionType == "Focus") NeonIris else NeonMint,
                                ringGradient = if (sessionType == "Focus") {
                                    listOf(NeonIris, Color(0xFF8B5CF6), NeonCyan)
                                } else {
                                    listOf(NeonMint, NeonCyan)
                                },
                                trackColor = Color(0x331E293B)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = formattedTime,
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.testTag("focus_countdown_text")
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isRunning) NeonMint.copy(alpha = 0.2f) else VoidSurfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isRunning) NeonMint.copy(alpha = 0.5f) else GlassBorder
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (isRunning) NeonMint else Color(0xFF94A3B8))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isRunning) "FLOW STATE ACTIVE" else "PAUSED",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            fontWeight = FontWeight.Bold,
                                            color = if (isRunning) NeonMint else Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Controls: Adjust (-5m), Big Play/Pause, Reset, Adjust (+5m)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedIconButton(
                                onClick = { onAdjustTime(-300) },
                                modifier = Modifier
                                    .size(46.dp)
                                    .testTag("timer_minus_5m")
                            ) {
                                Icon(imageVector = Icons.Filled.Remove, contentDescription = "Minus 5m", tint = Color.White)
                            }

                            FilledIconButton(
                                onClick = onToggleTimer,
                                modifier = Modifier
                                    .size(72.dp)
                                    .testTag("toggle_focus_timer_button"),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if (isRunning) NeonMint else NeonIris,
                                    contentColor = if (isRunning) Color.Black else Color.White
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
                                modifier = Modifier
                                    .size(46.dp)
                                    .testTag("reset_focus_timer_button")
                            ) {
                                Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Reset Timer", tint = Color.White)
                            }

                            OutlinedIconButton(
                                onClick = { onAdjustTime(300) },
                                modifier = Modifier
                                    .size(46.dp)
                                    .testTag("timer_plus_5m")
                            ) {
                                Icon(imageVector = Icons.Filled.Add, contentDescription = "Plus 5m", tint = Color.White)
                            }
                        }
                    }
                }
            }

            // Session Tag Selector
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Session Focus Domain",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = VoidSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.GraphicEq,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ambient Sonic Atmosphere",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(soundOptions) { sound ->
                                val isSelected = ambientSound == sound
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) NeonIris else VoidSurfaceVariant,
                                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(
                                        0.5.dp,
                                        GlassBorder
                                    ),
                                    modifier = Modifier
                                        .clickable { onSelectSound(sound) }
                                        .testTag("sound_option_$sound")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
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
                                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
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
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${todayMinutes}m logged",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonMint
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (recentSessions.isEmpty()) {
                        Surface(
                            color = VoidSurface.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No focus sessions recorded yet today. Hit start to log your first sprint!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        recentSessions.take(5).forEach { session ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = VoidSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(NeonIris)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = if (session.taskTitle.isNotBlank()) session.taskTitle else session.tag,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "${session.tag} • ${dateFormat.format(Date(session.completedAt))}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = NeonMint.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "+${session.durationMinutes}m",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonMint,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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
}
