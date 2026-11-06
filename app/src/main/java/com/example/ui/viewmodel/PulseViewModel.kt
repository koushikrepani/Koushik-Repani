package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DailyMoodEntity
import com.example.data.local.FocusSessionEntity
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.data.local.PulseDatabase
import com.example.data.local.SubtaskItem
import com.example.data.local.TaskEntity
import com.example.data.local.encodeSubtasks
import com.example.data.local.parseSubtasks
import com.example.data.repository.PulseRepository
import com.example.ui.navigation.Screen
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PulseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PulseRepository
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    init {
        val database = PulseDatabase.getDatabase(application, viewModelScope)
        repository = PulseRepository(database.appDao())
    }

    val todayDateStr: String = repository.getTodayDateString()

    // Navigation & Modals
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Today)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _showNewTaskSheet = MutableStateFlow(false)
    val showNewTaskSheet: StateFlow<Boolean> = _showNewTaskSheet.asStateFlow()

    private val _showNewHabitSheet = MutableStateFlow(false)
    val showNewHabitSheet: StateFlow<Boolean> = _showNewHabitSheet.asStateFlow()

    private val _showMoodDialog = MutableStateFlow(false)
    val showMoodDialog: StateFlow<Boolean> = _showMoodDialog.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    private val _selectedTaskDetail = MutableStateFlow<TaskEntity?>(null)
    val selectedTaskDetail: StateFlow<TaskEntity?> = _selectedTaskDetail.asStateFlow()

    private val _selectedHabitDetail = MutableStateFlow<HabitEntity?>(null)
    val selectedHabitDetail: StateFlow<HabitEntity?> = _selectedHabitDetail.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Preferences
    val userName = MutableStateFlow("Alex")
    val isHapticEnabled = MutableStateFlow(true)
    val isDarkMode = MutableStateFlow<Boolean?>(null) // null = system

    // Data Streams from Room
    val tasks: StateFlow<List<TaskEntity>> = repository.allTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val todayTasks: StateFlow<List<TaskEntity>> = repository.getTodayTasks().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val habits: StateFlow<List<HabitEntity>> = repository.allHabits.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val habitLogs: StateFlow<List<HabitLogEntity>> = repository.allHabitLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val todayCompletedHabitIds: StateFlow<Set<Long>> = repository.getHabitLogsForDate(todayDateStr).map { logs ->
        logs.map { it.habitId }.toSet()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptySet()
    )

    val focusSessions: StateFlow<List<FocusSessionEntity>> = repository.allFocusSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val todayFocusMinutes: StateFlow<Int> = repository.getTotalFocusMinutesToday().map { it ?: 0 }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val todayMood: StateFlow<DailyMoodEntity?> = repository.getMoodForDate(todayDateStr).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val allMoods: StateFlow<List<DailyMoodEntity>> = repository.allMoods.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Planner Filters
    val plannerSearchQuery = MutableStateFlow("")
    val plannerSelectedCategory = MutableStateFlow("All")
    val plannerSelectedPriority = MutableStateFlow("All")
    val plannerTab = MutableStateFlow(0) // 0: All, 1: Pending, 2: Completed

    // Habits Filter
    val habitsSelectedCategory = MutableStateFlow("All")

    // Focus Studio State
    val focusSessionType = MutableStateFlow("Focus") // "Focus", "Short Break", "Long Break"
    val focusTotalSeconds = MutableStateFlow(25 * 60)
    val focusRemainingSeconds = MutableStateFlow(25 * 60)
    val isFocusTimerRunning = MutableStateFlow(false)
    val focusTaskTitle = MutableStateFlow("")
    val focusSelectedTag = MutableStateFlow("Deep Work")
    val focusAmbientSound = MutableStateFlow("None") // "None", "Rain", "White Noise", "Binaural", "Cafe"

    private var timerJob: Job? = null

    // Navigation Methods
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun openNewTaskSheet() { _showNewTaskSheet.value = true }
    fun closeNewTaskSheet() { _showNewTaskSheet.value = false }

    fun openNewHabitSheet() { _showNewHabitSheet.value = true }
    fun closeNewHabitSheet() { _showNewHabitSheet.value = false }

    fun openMoodDialog() { _showMoodDialog.value = true }
    fun closeMoodDialog() { _showMoodDialog.value = false }

    fun openSettingsDialog() { _showSettingsDialog.value = true }
    fun closeSettingsDialog() { _showSettingsDialog.value = false }

    fun openTaskDetail(task: TaskEntity) { _selectedTaskDetail.value = task }
    fun closeTaskDetail() { _selectedTaskDetail.value = null }

    fun openHabitDetail(habit: HabitEntity) { _selectedHabitDetail.value = habit }
    fun closeHabitDetail() { _selectedHabitDetail.value = null }

    fun clearSnackbar() { _snackbarMessage.value = null }

    // Haptics Helper
    fun triggerHaptic(type: Int = 1) {
        if (!isHapticEnabled.value) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = when (type) {
                    2 -> VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                    3 -> VibrationEffect.createWaveform(longArrayOf(0, 80, 50, 80), -1)
                    else -> VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(40)
            }
        } catch (_: Exception) {}
    }

    // Task Actions
    fun addTask(
        title: String,
        description: String,
        category: String,
        priority: String,
        dueDate: Long,
        subtasks: List<SubtaskItem>
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val task = TaskEntity(
                title = title.trim(),
                description = description.trim(),
                category = category,
                priority = priority,
                dueDate = dueDate,
                subtasksRaw = encodeSubtasks(subtasks)
            )
            repository.insertTask(task)
            triggerHaptic(1)
            _snackbarMessage.value = "Task created: $title"
            closeNewTaskSheet()
        }
    }

    fun addTodayTask(title: String, category: String = "Work", priority: String = "Medium") {
        if (title.isBlank()) return
        viewModelScope.launch {
            val cal = java.util.Calendar.getInstance()
            cal.set(java.util.Calendar.HOUR_OF_DAY, 18)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            val dueMillis = if (cal.timeInMillis < System.currentTimeMillis()) {
                System.currentTimeMillis() + 1000 * 60 * 60 * 2
            } else {
                cal.timeInMillis
            }
            val task = TaskEntity(
                title = title.trim(),
                category = category,
                priority = priority,
                dueDate = dueMillis
            )
            repository.insertTask(task)
            triggerHaptic(1)
            _snackbarMessage.value = "Added to today's planner: $title"
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
            _selectedTaskDetail.value = task
            _snackbarMessage.value = "Task updated"
        }
    }

    fun toggleTaskComplete(task: TaskEntity) {
        viewModelScope.launch {
            val newStatus = !task.isCompleted
            repository.toggleTaskComplete(task.id, newStatus)
            triggerHaptic(if (newStatus) 2 else 1)
            _snackbarMessage.value = if (newStatus) "Task completed! 🎉" else "Task marked pending"
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            repository.deleteTask(id)
            closeTaskDetail()
            triggerHaptic(1)
            _snackbarMessage.value = "Task deleted"
        }
    }

    fun toggleSubtask(task: TaskEntity, index: Int) {
        val currentSubtasks = parseSubtasks(task.subtasksRaw).toMutableList()
        if (index in currentSubtasks.indices) {
            val item = currentSubtasks[index]
            currentSubtasks[index] = item.copy(isDone = !item.isDone)
            val updated = task.copy(subtasksRaw = encodeSubtasks(currentSubtasks))
            updateTask(updated)
            triggerHaptic(1)
        }
    }

    // Habit Actions
    fun addHabit(
        title: String,
        description: String,
        category: String,
        iconName: String,
        colorHex: String,
        targetDays: Int
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val habit = HabitEntity(
                title = title.trim(),
                description = description.trim(),
                category = category,
                iconName = iconName,
                colorHex = colorHex,
                targetDaysPerWeek = targetDays
            )
            repository.insertHabit(habit)
            triggerHaptic(1)
            _snackbarMessage.value = "Habit created: $title"
            closeNewHabitSheet()
        }
    }

    fun toggleHabitToday(habit: HabitEntity) {
        toggleHabitForDate(habit, todayDateStr)
    }

    fun toggleHabitForDate(habit: HabitEntity, dateString: String) {
        viewModelScope.launch {
            repository.toggleHabitForDate(habit, dateString)
            triggerHaptic(2)
        }
    }

    fun quickAddHabit(
        title: String,
        category: String = "Health",
        colorHex: String = "#6366F1",
        iconName: String = "water"
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val habit = HabitEntity(
                title = title.trim(),
                category = category,
                iconName = iconName,
                colorHex = colorHex,
                targetDaysPerWeek = 7
            )
            repository.insertHabit(habit)
            triggerHaptic(1)
            _snackbarMessage.value = "Habit created: $title"
        }
    }

    fun deleteHabit(id: Long) {
        viewModelScope.launch {
            repository.deleteHabit(id)
            closeHabitDetail()
            triggerHaptic(1)
            _snackbarMessage.value = "Habit removed"
        }
    }

    // Start Focus for specific task
    fun startFocusForTask(task: TaskEntity) {
        focusTaskTitle.value = task.title
        focusSelectedTag.value = task.category
        setFocusMode("Focus", 25)
        navigateTo(Screen.Focus)
        closeTaskDetail()
        _snackbarMessage.value = "Focus mode ready for '${task.title}'"
    }

    // Focus Studio Controls
    fun setFocusMode(type: String, minutes: Int) {
        pauseFocusTimer()
        focusSessionType.value = type
        focusTotalSeconds.value = minutes * 60
        focusRemainingSeconds.value = minutes * 60
    }

    fun toggleFocusTimer() {
        if (isFocusTimerRunning.value) {
            pauseFocusTimer()
        } else {
            startFocusTimer()
        }
    }

    private fun startFocusTimer() {
        isFocusTimerRunning.value = true
        triggerHaptic(1)
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isFocusTimerRunning.value && focusRemainingSeconds.value > 0) {
                delay(1000L)
                focusRemainingSeconds.value -= 1
            }
            if (focusRemainingSeconds.value <= 0) {
                onFocusTimerFinished()
            }
        }
    }

    fun pauseFocusTimer() {
        isFocusTimerRunning.value = false
        timerJob?.cancel()
        timerJob = null
    }

    fun resetFocusTimer() {
        pauseFocusTimer()
        focusRemainingSeconds.value = focusTotalSeconds.value
    }

    private fun onFocusTimerFinished() {
        pauseFocusTimer()
        triggerHaptic(3)
        val minutesCompleted = focusTotalSeconds.value / 60
        viewModelScope.launch {
            repository.recordFocusSession(
                FocusSessionEntity(
                    tag = focusSelectedTag.value,
                    taskTitle = focusTaskTitle.value,
                    durationMinutes = minutesCompleted,
                    sessionType = focusSessionType.value
                )
            )
            _snackbarMessage.value = "✨ Focus session completed! +${minutesCompleted}m logged"
            // Reset to short break or next focus
            if (focusSessionType.value == "Focus") {
                setFocusMode("Short Break", 5)
            } else {
                setFocusMode("Focus", 25)
            }
        }
    }

    // Mood & Reflection
    fun saveMood(emoji: String, score: Int, energy: Int, note: String) {
        viewModelScope.launch {
            repository.logMood(
                DailyMoodEntity(
                    dateString = todayDateStr,
                    moodEmoji = emoji,
                    moodScore = score,
                    energyScore = energy,
                    reflectionNote = note.trim()
                )
            )
            triggerHaptic(2)
            _snackbarMessage.value = "Daily reflection saved 🌟"
            closeMoodDialog()
        }
    }

    // Reset starter data
    fun resetStarterData() {
        viewModelScope.launch {
            val db = PulseDatabase.getDatabase(getApplication(), viewModelScope)
            PulseDatabase.prepopulateDatabase(db.appDao())
            _snackbarMessage.value = "Sample routines reloaded!"
            closeSettingsDialog()
        }
    }
}
