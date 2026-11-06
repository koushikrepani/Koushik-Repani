package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- Tasks ---
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, priority = 'Urgent' DESC, priority = 'High' DESC, dueDate ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE dueDate >= :startOfDay AND dueDate <= :endOfDay ORDER BY isCompleted ASC, priority = 'Urgent' DESC, priority = 'High' DESC, dueDate ASC")
    fun getTasksForDateRange(startOfDay: Long, endOfDay: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("UPDATE tasks SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :id")
    suspend fun setTaskCompleted(id: Long, isCompleted: Boolean, completedAt: Long?)

    // --- Habits ---
    @Query("SELECT * FROM habits ORDER BY currentStreak DESC, id DESC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabitById(id: Long)

    // --- Habit Logs ---
    @Query("SELECT * FROM habit_logs WHERE dateString = :dateString")
    fun getHabitLogsForDate(dateString: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE dateString >= :startDate AND dateString <= :endDate")
    fun getHabitLogsForDateRange(startDate: String, endDate: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId")
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs ORDER BY completedAt DESC")
    fun getAllHabitLogs(): Flow<List<HabitLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitLog(log: HabitLogEntity): Long

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND dateString = :dateString")
    suspend fun deleteHabitLog(habitId: Long, dateString: String)

    @Query("SELECT COUNT(*) FROM habit_logs WHERE habitId = :habitId AND dateString = :dateString")
    suspend fun isHabitCompletedOnDate(habitId: Long, dateString: String): Int

    // --- Focus Sessions ---
    @Query("SELECT * FROM focus_sessions ORDER BY completedAt DESC")
    fun getAllFocusSessions(): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSessionEntity): Long

    @Query("SELECT SUM(durationMinutes) FROM focus_sessions WHERE sessionType = 'Focus' AND completedAt >= :sinceTimestamp")
    fun getTotalFocusMinutesSince(sinceTimestamp: Long): Flow<Int?>

    // --- Mood / Reflections ---
    @Query("SELECT * FROM daily_moods ORDER BY dateString DESC")
    fun getAllMoods(): Flow<List<DailyMoodEntity>>

    @Query("SELECT * FROM daily_moods WHERE dateString = :dateString LIMIT 1")
    fun getMoodForDate(dateString: String): Flow<DailyMoodEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(mood: DailyMoodEntity): Long
}
