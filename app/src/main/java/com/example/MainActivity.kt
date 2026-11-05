package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.HabitDetailDialog
import com.example.ui.components.MoodLoggerDialog
import com.example.ui.components.NewHabitSheet
import com.example.ui.components.NewTaskSheet
import com.example.ui.components.QuickActionSpeedDial
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TaskDetailDialog
import com.example.ui.navigation.PulseBottomBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.analytics.AnalyticsScreen
import com.example.ui.screens.focus.FocusScreen
import com.example.ui.screens.habits.HabitsScreen
import com.example.ui.screens.planner.PlannerScreen
import com.example.ui.screens.today.TodayScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PulseViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val pulseViewModel: PulseViewModel = viewModel()
            val isDarkThemePref by pulseViewModel.isDarkMode.collectAsStateWithLifecycle()
            val useDark = isDarkThemePref ?: isSystemInDarkTheme()

            MyApplicationTheme(darkTheme = useDark) {
                PulseApp(viewModel = pulseViewModel)
            }
        }
    }
}

@Composable
fun PulseApp(viewModel: PulseViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val todayTasks by viewModel.todayTasks.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val habitLogs by viewModel.habitLogs.collectAsStateWithLifecycle()
    val todayCompletedHabitIds by viewModel.todayCompletedHabitIds.collectAsStateWithLifecycle()
    val focusSessions by viewModel.focusSessions.collectAsStateWithLifecycle()
    val todayFocusMinutes by viewModel.todayFocusMinutes.collectAsStateWithLifecycle()
    val todayMood by viewModel.todayMood.collectAsStateWithLifecycle()
    val allMoods by viewModel.allMoods.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val isHapticEnabled by viewModel.isHapticEnabled.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    // Modals
    val showNewTaskSheet by viewModel.showNewTaskSheet.collectAsStateWithLifecycle()
    val showNewHabitSheet by viewModel.showNewHabitSheet.collectAsStateWithLifecycle()
    val showMoodDialog by viewModel.showMoodDialog.collectAsStateWithLifecycle()
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsStateWithLifecycle()
    val selectedTaskDetail by viewModel.selectedTaskDetail.collectAsStateWithLifecycle()
    val selectedHabitDetail by viewModel.selectedHabitDetail.collectAsStateWithLifecycle()

    // Planner state
    val searchQuery by viewModel.plannerSearchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.plannerSelectedCategory.collectAsStateWithLifecycle()
    val selectedPriority by viewModel.plannerSelectedPriority.collectAsStateWithLifecycle()
    val selectedTab by viewModel.plannerTab.collectAsStateWithLifecycle()

    // Habits filter state
    val habitsSelectedCategory by viewModel.habitsSelectedCategory.collectAsStateWithLifecycle()

    // Focus state
    val focusSessionType by viewModel.focusSessionType.collectAsStateWithLifecycle()
    val focusTotalSeconds by viewModel.focusTotalSeconds.collectAsStateWithLifecycle()
    val focusRemainingSeconds by viewModel.focusRemainingSeconds.collectAsStateWithLifecycle()
    val isFocusRunning by viewModel.isFocusTimerRunning.collectAsStateWithLifecycle()
    val focusTaskTitle by viewModel.focusTaskTitle.collectAsStateWithLifecycle()
    val focusTag by viewModel.focusSelectedTag.collectAsStateWithLifecycle()
    val focusAmbientSound by viewModel.focusAmbientSound.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    // Seamless Back Navigation handling
    BackHandler(enabled = currentScreen != Screen.Today) {
        viewModel.navigateTo(Screen.Today)
    }

    val pendingTasksCount = tasks.count { !it.isCompleted }
    val uncompletedHabitsCount = habits.count { !todayCompletedHabitIds.contains(it.id) }

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("pulse_main_scaffold"),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            PulseBottomBar(
                currentScreen = currentScreen,
                pendingTasksCount = pendingTasksCount,
                uncompletedHabitsCount = uncompletedHabitsCount,
                isTimerRunning = isFocusRunning,
                onNavigate = { viewModel.navigateTo(it) }
            )
        },
        floatingActionButton = {
            if (currentScreen != Screen.Focus) {
                QuickActionSpeedDial(
                    onNewTask = { viewModel.openNewTaskSheet() },
                    onNewHabit = { viewModel.openNewHabitSheet() },
                    onQuickFocus = {
                        viewModel.setFocusMode("Focus", 25)
                        viewModel.navigateTo(Screen.Focus)
                    },
                    onMoodLog = { viewModel.openMoodDialog() }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = currentScreen,
                label = "ScreenCrossfade"
            ) { screen ->
                when (screen) {
                    Screen.Today -> TodayScreen(
                        userName = userName,
                        tasks = tasks,
                        todayTasks = todayTasks,
                        habits = habits,
                        todayCompletedHabitIds = todayCompletedHabitIds,
                        todayFocusMinutes = todayFocusMinutes,
                        todayMood = todayMood,
                        onNavigate = { viewModel.navigateTo(it) },
                        onOpenSettings = { viewModel.openSettingsDialog() },
                        onOpenMoodLog = { viewModel.openMoodDialog() },
                        onToggleHabit = { viewModel.toggleHabitToday(it) },
                        onToggleTask = { viewModel.toggleTaskComplete(it) },
                        onTaskClick = { viewModel.openTaskDetail(it) },
                        onHabitClick = { viewModel.openHabitDetail(it) },
                        onNewTask = { viewModel.openNewTaskSheet() },
                        onQuickAddTodayTask = { title, cat, prio -> viewModel.addTodayTask(title, cat, prio) },
                        onStartFocus = { viewModel.startFocusForTask(it) }
                    )

                    Screen.Planner -> PlannerScreen(
                        tasks = tasks,
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory,
                        selectedPriority = selectedPriority,
                        selectedTab = selectedTab,
                        onSearchChange = { viewModel.plannerSearchQuery.value = it },
                        onCategoryChange = { viewModel.plannerSelectedCategory.value = it },
                        onPriorityChange = { viewModel.plannerSelectedPriority.value = it },
                        onTabChange = { viewModel.plannerTab.value = it },
                        onToggleTaskComplete = { viewModel.toggleTaskComplete(it) },
                        onTaskClick = { viewModel.openTaskDetail(it) },
                        onStartFocus = { viewModel.startFocusForTask(it) },
                        onNewTask = { viewModel.openNewTaskSheet() }
                    )

                    Screen.Habits -> HabitsScreen(
                        habits = habits,
                        habitLogs = habitLogs,
                        todayCompletedHabitIds = todayCompletedHabitIds,
                        selectedCategory = habitsSelectedCategory,
                        onCategoryChange = { viewModel.habitsSelectedCategory.value = it },
                        onToggleHabitToday = { viewModel.toggleHabitToday(it) },
                        onHabitClick = { viewModel.openHabitDetail(it) },
                        onNewHabit = { viewModel.openNewHabitSheet() }
                    )

                    Screen.Focus -> FocusScreen(
                        sessionType = focusSessionType,
                        totalSeconds = focusTotalSeconds,
                        remainingSeconds = focusRemainingSeconds,
                        isRunning = isFocusRunning,
                        taskTitle = focusTaskTitle,
                        selectedTag = focusTag,
                        ambientSound = focusAmbientSound,
                        todayMinutes = todayFocusMinutes,
                        recentSessions = focusSessions,
                        onSelectMode = { type, minutes -> viewModel.setFocusMode(type, minutes) },
                        onToggleTimer = { viewModel.toggleFocusTimer() },
                        onResetTimer = { viewModel.resetFocusTimer() },
                        onAdjustTime = { delta ->
                            val updated = (viewModel.focusRemainingSeconds.value + delta).coerceAtLeast(60)
                            viewModel.focusRemainingSeconds.value = updated
                            viewModel.focusTotalSeconds.value = updated
                        },
                        onSelectTag = { viewModel.focusSelectedTag.value = it },
                        onSelectSound = { viewModel.focusAmbientSound.value = it }
                    )

                    Screen.Analytics -> AnalyticsScreen(
                        tasks = tasks,
                        habits = habits,
                        habitLogs = habitLogs,
                        focusSessions = focusSessions,
                        moods = allMoods
                    )
                }
            }
        }
    }

    // Modal Overlays
    if (showNewTaskSheet) {
        NewTaskSheet(
            onDismiss = { viewModel.closeNewTaskSheet() },
            onSaveTask = { title, desc, cat, prio, due, subtasks ->
                viewModel.addTask(title, desc, cat, prio, due, subtasks)
            }
        )
    }

    if (showNewHabitSheet) {
        NewHabitSheet(
            onDismiss = { viewModel.closeNewHabitSheet() },
            onSaveHabit = { title, desc, cat, icon, colorHex, targetDays ->
                viewModel.addHabit(title, desc, cat, icon, colorHex, targetDays)
            }
        )
    }

    selectedTaskDetail?.let { task ->
        TaskDetailDialog(
            task = task,
            onDismiss = { viewModel.closeTaskDetail() },
            onToggleTaskComplete = { viewModel.toggleTaskComplete(it) },
            onToggleSubtask = { t, idx -> viewModel.toggleSubtask(t, idx) },
            onStartFocus = { viewModel.startFocusForTask(it) },
            onDeleteTask = { viewModel.deleteTask(it) }
        )
    }

    selectedHabitDetail?.let { habit ->
        val logsForHabit = habitLogs.filter { it.habitId == habit.id }
        val isDoneToday = todayCompletedHabitIds.contains(habit.id)
        HabitDetailDialog(
            habit = habit,
            habitLogs = logsForHabit,
            isCompletedToday = isDoneToday,
            onDismiss = { viewModel.closeHabitDetail() },
            onToggleToday = { viewModel.toggleHabitToday(it) },
            onDeleteHabit = { viewModel.deleteHabit(it) }
        )
    }

    if (showMoodDialog) {
        MoodLoggerDialog(
            existingMood = todayMood,
            onDismiss = { viewModel.closeMoodDialog() },
            onSaveMood = { emoji, score, energy, note ->
                viewModel.saveMood(emoji, score, energy, note)
            }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            currentName = userName,
            isHapticEnabled = isHapticEnabled,
            isDarkMode = isDarkMode,
            onUpdateName = { viewModel.userName.value = it },
            onToggleHaptic = { viewModel.isHapticEnabled.value = it },
            onSetThemeMode = { viewModel.isDarkMode.value = it },
            onResetData = { viewModel.resetStarterData() },
            onDismiss = { viewModel.closeSettingsDialog() }
        )
    }
}
