package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Today : Screen(
        route = "today",
        title = "Today",
        selectedIcon = Icons.Filled.RadioButtonChecked,
        unselectedIcon = Icons.Outlined.RadioButtonUnchecked
    )

    data object Planner : Screen(
        route = "planner",
        title = "Planner",
        selectedIcon = Icons.Filled.DateRange,
        unselectedIcon = Icons.Outlined.DateRange
    )

    data object Habits : Screen(
        route = "habits",
        title = "Habits",
        selectedIcon = Icons.Filled.ElectricBolt,
        unselectedIcon = Icons.Outlined.ElectricBolt
    )

    data object Focus : Screen(
        route = "focus",
        title = "Focus",
        selectedIcon = Icons.Filled.Timer,
        unselectedIcon = Icons.Outlined.Timer
    )

    data object Analytics : Screen(
        route = "analytics",
        title = "Insights",
        selectedIcon = Icons.Filled.Insights,
        unselectedIcon = Icons.Outlined.Insights
    )

    companion object {
        val bottomNavItems: List<Screen>
            get() = listOf(Today, Planner, Habits, Focus, Analytics)
    }
}
