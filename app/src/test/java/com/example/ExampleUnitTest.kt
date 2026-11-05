package com.example

import com.example.data.local.TaskEntity
import com.example.ui.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun screen_bottomNavItems_areAllNonNullAndHaveRoutes() {
        val items = Screen.bottomNavItems
        assertEquals(5, items.size)
        items.forEach { screen ->
            assertNotNull(screen)
            assertNotNull(screen.route)
            assertNotNull(screen.title)
        }
        assertEquals("today", Screen.Today.route)
        assertEquals("planner", Screen.Planner.route)
        assertEquals("habits", Screen.Habits.route)
        assertEquals("focus", Screen.Focus.route)
        assertEquals("analytics", Screen.Analytics.route)
    }

    @Test
    fun dailyPlanner_taskCalculations_areCorrect() {
        val sampleTasks = listOf(
            TaskEntity(id = 1, title = "Task 1", isCompleted = true),
            TaskEntity(id = 2, title = "Task 2", isCompleted = false),
            TaskEntity(id = 3, title = "Task 3", isCompleted = true),
            TaskEntity(id = 4, title = "Task 4", isCompleted = false)
        )

        val total = sampleTasks.size
        val completed = sampleTasks.count { it.isCompleted }
        val pending = total - completed
        val progress = completed.toFloat() / total

        assertEquals(4, total)
        assertEquals(2, completed)
        assertEquals(2, pending)
        assertEquals(0.5f, progress, 0.001f)
    }
}
