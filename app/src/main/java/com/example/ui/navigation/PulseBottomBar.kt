package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NeonIris
import com.example.ui.theme.VoidSurface

@Composable
fun PulseBottomBar(
    currentScreen: Screen,
    pendingTasksCount: Int,
    uncompletedHabitsCount: Int,
    isTimerRunning: Boolean,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .testTag("pulse_bottom_navigation_bar")
            .windowInsetsPadding(WindowInsets.navigationBars)
            .border(
                width = 0.5.dp,
                color = Color(0x336366F1)
            ),
        containerColor = VoidSurface.copy(alpha = 0.94f),
        tonalElevation = 8.dp
    ) {
        Screen.bottomNavItems.forEach { screen ->
            val isSelected = screen == currentScreen

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(screen) },
                icon = {
                    BadgedBox(
                        badge = {
                            when (screen) {
                                Screen.Planner -> {
                                    if (pendingTasksCount > 0) {
                                        Badge(
                                            containerColor = NeonIris,
                                            contentColor = Color.White
                                        ) {
                                            Text(text = if (pendingTasksCount > 9) "9+" else "$pendingTasksCount")
                                        }
                                    }
                                }
                                Screen.Habits -> {
                                    if (uncompletedHabitsCount > 0) {
                                        Badge(
                                            containerColor = Color(0xFFFF9800),
                                            contentColor = Color.Black
                                        ) {
                                            Text(text = "$uncompletedHabitsCount")
                                        }
                                    }
                                }
                                Screen.Focus -> {
                                    if (isTimerRunning) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981))
                                        )
                                    }
                                }
                                else -> {}
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                            contentDescription = screen.title
                        )
                    }
                },
                label = {
                    Text(
                        text = screen.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = NeonIris,
                    indicatorColor = NeonIris.copy(alpha = 0.28f),
                    unselectedIconColor = Color(0xFF64748B),
                    unselectedTextColor = Color(0xFF64748B)
                ),
                modifier = Modifier.testTag("nav_item_${screen.route}")
            )
        }
    }
}
