package com.example.data.repository

import com.example.data.local.AppDao
import com.example.data.local.DailyMoodEntity
import com.example.data.local.FocusSessionEntity
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.data.local.TaskEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PulseRepository(private val appDao: AppDao) {

    val allTasks: Flow<List<TaskEntity>> = appDao.getAllTasks()
    val allHabits: Flow<List<HabitEntity>> = appDao.getAllHabits()
    val allHabitLogs: Flow<List<HabitLogEntity>> = appDao.getAllHabitLogs()
    val allFocusSessions: Flow<List<FocusSessionEntity>> = appDao.getAllFocusSessions()
    val allMoods: Flow<List<DailyMoodEntity>> = appDao.getAllMoods()

    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    fun getTodayTasks(): Flow<List<TaskEntity>> {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis

        cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
        cal.set(java.util.Calendar.MINUTE, 59)
        cal.set(java.util.Calendar.SECOND, 59)
        cal.set(java.util.Calendar.MILLISECOND, 999)
        val endOfDay = cal.timeInMillis

        return appDao.getTasksForDateRange(startOfDay, endOfDay)
    }

    fun getHabitLogsForDate(dateString: String): Flow<List<HabitLogEntity>> {
        return appDao.getHabitLogsForDate(dateString)
    }

    fun getLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>> {
        return appDao.getLogsForHabit(habitId)
    }

    fun getMoodForDate(dateString: String): Flow<DailyMoodEntity?> {
        return appDao.getMoodForDate(dateString)
    }

    fun getTotalFocusMinutesToday(): Flow<Int?> {
        val startOfToday = getStartOfTodayMillis()
        return appDao.getTotalFocusMinutesSince(startOfToday)
    }

    private fun getStartOfTodayMillis(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    // Task Actions
    suspend fun insertTask(task: TaskEntity): Long = appDao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = appDao.updateTask(task)
    suspend fun deleteTask(id: Long) = appDao.deleteTaskById(id)
    suspend fun toggleTaskComplete(id: Long, completed: Boolean) {
        val completedAt = if (completed) System.currentTimeMillis() else null
        appDao.setTaskCompleted(id, completed, completedAt)
    }

    // Habit Actions
    suspend fun insertHabit(habit: HabitEntity): Long = appDao.insertHabit(habit)
    suspend fun updateHabit(habit: HabitEntity) = appDao.updateHabit(habit)
    suspend fun deleteHabit(id: Long) = appDao.deleteHabitById(id)

    suspend fun toggleHabitForDate(habit: HabitEntity, dateString: String) {
        val isDone = appDao.isHabitCompletedOnDate(habit.id, dateString) > 0
        if (isDone) {
            appDao.deleteHabitLog(habit.id, dateString)
            // decrement total and streak
            val newStreak = (habit.currentStreak - 1).coerceAtLeast(0)
            val newTotal = (habit.totalCompletions - 1).coerceAtLeast(0)
            appDao.updateHabit(habit.copy(currentStreak = newStreak, totalCompletions = newTotal))
        } else {
            appDao.insertHabitLog(HabitLogEntity(habitId = habit.id, dateString = dateString))
            val newStreak = habit.currentStreak + 1
            val best = maxOf(newStreak, habit.bestStreak)
            val newTotal = habit.totalCompletions + 1
            appDao.updateHabit(habit.copy(currentStreak = newStreak, bestStreak = best, totalCompletions = newTotal))
        }
    }

    // Focus Session Actions
    suspend fun recordFocusSession(session: FocusSessionEntity): Long = appDao.insertFocusSession(session)

    // Mood Actions
    suspend fun logMood(mood: DailyMoodEntity): Long = appDao.insertMood(mood)
}
