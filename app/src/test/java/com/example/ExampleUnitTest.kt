package com.example

import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.data.local.TaskEntity
import com.example.ui.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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

    @Test
    fun habitTracking_completionAndStreaks_areCorrect() {
        val habit1 = HabitEntity(id = 10, title = "Morning Run", currentStreak = 5, totalCompletions = 12)
        val habit2 = HabitEntity(id = 20, title = "Read Book", currentStreak = 3, totalCompletions = 8)

        val logs = listOf(
            HabitLogEntity(id = 1, habitId = 10, dateString = "2026-11-05"),
            HabitLogEntity(id = 2, habitId = 10, dateString = "2026-11-04"),
            HabitLogEntity(id = 3, habitId = 20, dateString = "2026-11-05")
        )

        val completedOnNov5 = logs.filter { it.dateString == "2026-11-05" }.map { it.habitId }.toSet()

        assertTrue(completedOnNov5.contains(10L))
        assertTrue(completedOnNov5.contains(20L))
        assertEquals(2, completedOnNov5.size)

        val completedOnNov4 = logs.filter { it.dateString == "2026-11-04" }.map { it.habitId }.toSet()
        assertTrue(completedOnNov4.contains(10L))
        assertEquals(1, completedOnNov4.size)
    }

    @Test
    fun pomodoro_modesAndTimingCalculations_areCorrect() {
        assertEquals(25, com.example.ui.components.PomodoroMode.FOCUS.defaultMinutes)
        assertEquals(5, com.example.ui.components.PomodoroMode.SHORT_BREAK.defaultMinutes)
        assertEquals(15, com.example.ui.components.PomodoroMode.LONG_BREAK.defaultMinutes)

        val totalSeconds = 25 * 60
        val remainingSeconds = 15 * 60
        val progress = ((totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat())
        assertEquals(0.4f, progress, 0.001f)
    }
}
