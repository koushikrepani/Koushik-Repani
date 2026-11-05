package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        TaskEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        FocusSessionEntity::class,
        DailyMoodEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PulseDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: PulseDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): PulseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PulseDatabase::class.java,
                    "pulse_database"
                )
                .addCallback(PulseDatabaseCallback(scope))
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class PulseDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        prepopulateDatabase(database.appDao())
                    }
                }
            }
        }

        suspend fun prepopulateDatabase(dao: AppDao) {
            val now = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val todayStr = dateFormat.format(Date(now))

            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = dateFormat.format(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val dayBeforeStr = dateFormat.format(cal.time)

            // Seed Habits
            val habit1 = HabitEntity(
                title = "Morning Hydration & Sunlight",
                description = "Drink 500ml water and get 10 minutes of direct morning sunlight",
                category = "Health",
                iconName = "water",
                colorHex = "#0EA5E9",
                targetDaysPerWeek = 7,
                currentStreak = 4,
                bestStreak = 12,
                totalCompletions = 18
            )
            val habit2 = HabitEntity(
                title = "Read 15 Pages",
                description = "Deep non-fiction or philosophy reading without phone distractions",
                category = "Mind",
                iconName = "book",
                colorHex = "#8B5CF6",
                targetDaysPerWeek = 6,
                currentStreak = 3,
                bestStreak = 9,
                totalCompletions = 14
            )
            val habit3 = HabitEntity(
                title = "Cardio or Movement Flow",
                description = "30-min run, brisk walk, or high intensity functional training",
                category = "Fitness",
                iconName = "workout",
                colorHex = "#F43F5E",
                targetDaysPerWeek = 5,
                currentStreak = 2,
                bestStreak = 7,
                totalCompletions = 11
            )
            val habit4 = HabitEntity(
                title = "Evening Wind Down & Journal",
                description = "Review wins, set top 3 goals for tomorrow, avoid screens 30m before bed",
                category = "Wellness",
                iconName = "meditation",
                colorHex = "#10B981",
                targetDaysPerWeek = 7,
                currentStreak = 5,
                bestStreak = 15,
                totalCompletions = 22
            )

            val h1Id = dao.insertHabit(habit1)
            val h2Id = dao.insertHabit(habit2)
            val h3Id = dao.insertHabit(habit3)
            val h4Id = dao.insertHabit(habit4)

            // Seed Habit Logs for streaks
            dao.insertHabitLog(HabitLogEntity(habitId = h1Id, dateString = todayStr))
            dao.insertHabitLog(HabitLogEntity(habitId = h1Id, dateString = yesterdayStr))
            dao.insertHabitLog(HabitLogEntity(habitId = h1Id, dateString = dayBeforeStr))

            dao.insertHabitLog(HabitLogEntity(habitId = h2Id, dateString = yesterdayStr))
            dao.insertHabitLog(HabitLogEntity(habitId = h2Id, dateString = dayBeforeStr))

            dao.insertHabitLog(HabitLogEntity(habitId = h4Id, dateString = yesterdayStr))
            dao.insertHabitLog(HabitLogEntity(habitId = h4Id, dateString = dayBeforeStr))

            // Seed Tasks
            dao.insertTask(
                TaskEntity(
                    title = "Review Mobile Architecture & Navigation",
                    description = "Ensure fluid back navigation, responsive bottom navigation, and zero dead taps",
                    category = "Work",
                    priority = "Urgent",
                    dueDate = now + 1000 * 60 * 60 * 3, // 3 hours from now
                    subtasksRaw = "1:Check back handler on sub-screens\n1:Verify touch targets 48dp\n0:Test adaptive layout on tablet"
                )
            )
            dao.insertTask(
                TaskEntity(
                    title = "Prepare Weekly Sprint Briefing",
                    description = "Highlight milestone achievements, blocker resolutions, and upcoming target dates",
                    category = "Work",
                    priority = "High",
                    dueDate = now + 1000 * 60 * 60 * 6,
                    subtasksRaw = "0:Extract metrics\n0:Format slide deck"
                )
            )
            dao.insertTask(
                TaskEntity(
                    title = "Grocery & Meal Prep Shopping",
                    description = "Fresh produce, lean proteins, herbal teas, and healthy snacks",
                    category = "Personal",
                    priority = "Medium",
                    dueDate = now + 1000 * 60 * 60 * 8,
                    subtasksRaw = "0:Almonds & Greek yogurt\n0:Spinach & blueberries\n0:Avocados"
                )
            )
            dao.insertTask(
                TaskEntity(
                    title = "Schedule Annual Health Checkup",
                    description = "Call clinic and confirm morning appointment slot",
                    category = "Health",
                    priority = "Low",
                    dueDate = now + 1000 * 60 * 60 * 24
                )
            )

            // Seed Focus Sessions
            dao.insertFocusSession(
                FocusSessionEntity(
                    tag = "Deep Work",
                    taskTitle = "Mobile Architecture Review",
                    durationMinutes = 25,
                    sessionType = "Focus",
                    completedAt = now - 1000 * 60 * 90
                )
            )
            dao.insertFocusSession(
                FocusSessionEntity(
                    tag = "Deep Work",
                    taskTitle = "Mobile Architecture Review",
                    durationMinutes = 25,
                    sessionType = "Focus",
                    completedAt = now - 1000 * 60 * 45
                )
            )

            // Seed Today's Mood
            dao.insertMood(
                DailyMoodEntity(
                    dateString = todayStr,
                    moodEmoji = "⚡ Energetic",
                    moodScore = 5,
                    energyScore = 4,
                    reflectionNote = "Ready to tackle the roadmap with clear focus and high energy."
                )
            )
        }
    }
}
