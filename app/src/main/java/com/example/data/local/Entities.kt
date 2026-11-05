package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "Work",
    val priority: String = "Medium", // Urgent, High, Medium, Low
    val dueDate: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val subtasksRaw: String = "", // formatted string "0:Item 1\n1:Item 2" (0=incomplete, 1=complete)
    val createdAt: Long = System.currentTimeMillis()
)

data class SubtaskItem(
    val title: String,
    val isDone: Boolean
)

fun parseSubtasks(raw: String): List<SubtaskItem> {
    if (raw.isBlank()) return emptyList()
    return raw.lines().filter { it.isNotBlank() }.map { line ->
        if (line.startsWith("1:")) {
            SubtaskItem(line.substring(2), true)
        } else if (line.startsWith("0:")) {
            SubtaskItem(line.substring(2), false)
        } else {
            SubtaskItem(line, false)
        }
    }
}

fun encodeSubtasks(items: List<SubtaskItem>): String {
    return items.joinToString("\n") { (if (it.isDone) "1:" else "0:") + it.title }
}

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "Wellness",
    val iconName: String = "sparkle",
    val colorHex: String = "#6366F1",
    val targetDaysPerWeek: Int = 7,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalCompletions: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "habit_logs")
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val dateString: String, // "YYYY-MM-DD"
    val completedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tag: String = "Deep Work",
    val taskTitle: String = "",
    val durationMinutes: Int = 25,
    val sessionType: String = "Focus", // Focus, Short Break, Long Break
    val completedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_moods")
data class DailyMoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateString: String,
    val moodEmoji: String,
    val moodScore: Int, // 1 to 5
    val energyScore: Int, // 1 to 5
    val reflectionNote: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
