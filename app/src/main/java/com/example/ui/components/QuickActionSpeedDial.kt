package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PulseIndigoPrimary

@Composable
fun QuickActionSpeedDial(
    onNewTask: () -> Unit,
    onNewHabit: () -> Unit,
    onQuickFocus: () -> Unit,
    onMoodLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.testTag("quick_action_speed_dial"),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SpeedDialItem(
                    label = "Log Daily Mood",
                    icon = Icons.Filled.Mood,
                    containerColor = Color(0xFF06B6D4),
                    contentColor = Color.White,
                    testTag = "speed_dial_mood",
                    onClick = {
                        expanded = false
                        onMoodLog()
                    }
                )

                SpeedDialItem(
                    label = "Quick Focus",
                    icon = Icons.Filled.Timer,
                    containerColor = Color(0xFF8B5CF6),
                    contentColor = Color.White,
                    testTag = "speed_dial_focus",
                    onClick = {
                        expanded = false
                        onQuickFocus()
                    }
                )

                SpeedDialItem(
                    label = "New Habit",
                    icon = Icons.Filled.ElectricBolt,
                    containerColor = Color(0xFFF97316),
                    contentColor = Color.White,
                    testTag = "speed_dial_habit",
                    onClick = {
                        expanded = false
                        onNewHabit()
                    }
                )

                SpeedDialItem(
                    label = "New Task",
                    icon = Icons.Filled.DateRange,
                    containerColor = PulseIndigoPrimary,
                    contentColor = Color.White,
                    testTag = "speed_dial_task",
                    onClick = {
                        expanded = false
                        onNewTask()
                    }
                )
            }
        }

        FloatingActionButton(
            onClick = { expanded = !expanded },
            containerColor = if (expanded) MaterialTheme.colorScheme.surfaceVariant else PulseIndigoPrimary,
            contentColor = if (expanded) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(6.dp),
            modifier = Modifier.testTag("fab_quick_action_main")
        ) {
            Icon(
                imageVector = if (expanded) Icons.Filled.Close else Icons.Filled.Add,
                contentDescription = if (expanded) "Close actions" else "Quick actions"
            )
        }
    }
}

@Composable
private fun SpeedDialItem(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            border = androidx.compose.foundation.BorderStroke(
                0.5.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            ),
            modifier = Modifier.padding(end = 6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }

        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = containerColor,
            contentColor = contentColor,
            shape = CircleShape,
            modifier = Modifier.testTag(testTag)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
