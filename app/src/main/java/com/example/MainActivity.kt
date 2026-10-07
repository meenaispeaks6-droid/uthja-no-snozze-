package com.example

import android.Manifest
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Toast
import com.example.admin.AntiCheatProtectionManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.alarm.AlarmScheduler
import com.example.data.model.AlarmEntity
import com.example.data.model.DismissType
import com.example.data.model.WakeMode
import com.example.ui.LuneViewModel
import com.example.ui.components.LuneBottomNavigation
import com.example.ui.components.LuneTab
import com.example.ui.dialogs.AlarmEditDialog
import com.example.ui.dialogs.AlarmEditScreen
import com.example.ui.screens.ActiveAlarmScreen
import com.example.ui.screens.AlarmsScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.ColorTilesMissionScreen
import com.example.ui.screens.HabitAlarmScreen
import com.example.ui.screens.MathMissionScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SleepScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.TypingMissionScreen
import com.example.ui.screens.WakeUpMissionPickerScreen
import com.example.ui.screens.WallpaperAlarmRingingScreen
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.ThemeMode
import androidx.compose.material3.MaterialTheme
import com.example.ui.theme.LuneBackground
import com.example.ui.theme.LuneColors
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: LuneViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AntiCheatProtectionManager.init(this)
        enableEdgeToEdge()
        enableFullScreen()
        applyLockScreenAndWakeFlags()

        checkAlarmIntent(intent)

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val systemInDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                ThemeMode.SYSTEM -> systemInDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            MyApplicationTheme(darkTheme = isDark) {
                LuneApp(
                    viewModel = viewModel,
                    themeMode = themeMode,
                    onThemeModeChanged = { viewModel.setThemeMode(it) }
                )
            }
        }
    }

    private fun applyLockScreenAndWakeFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        }
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )
    }

    override fun onResume() {
        super.onResume()
        enableFullScreen()
        if (viewModel.activeAlarm.value.isRinging) {
            applyLockScreenAndWakeFlags()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            enableFullScreen()
        } else {
            val isAlarmRinging = viewModel.activeAlarm.value.isRinging && !viewModel.activeAlarm.value.isConquered
            if (isAlarmRinging && AntiCheatProtectionManager.isPreventPowerOffEnabled.value) {
                // System power dialog or status bar pulled down during active alarm
                AntiCheatProtectionManager.recordPowerOffBlockAttempt(this, "Window focus lost to system dialog")
                val bringToFront = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                try {
                    startActivity(bringToFront)
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val isAlarmRinging = viewModel.activeAlarm.value.isRinging && !viewModel.activeAlarm.value.isConquered
        if (isAlarmRinging && AntiCheatProtectionManager.isPreventPowerOffEnabled.value) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_POWER,
                KeyEvent.KEYCODE_VOLUME_DOWN,
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        AntiCheatProtectionManager.recordPowerOffBlockAttempt(this, "Hardware button pressed during alarm")
                        Toast.makeText(this, "🔒 Power-off is disabled during wake-up mission!", Toast.LENGTH_SHORT).show()
                    }
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun enableFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        @Suppress("DEPRECATION")
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        @Suppress("DEPRECATION")
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        applyLockScreenAndWakeFlags()
        checkAlarmIntent(intent)
    }

    private fun checkAlarmIntent(intent: Intent?) {
        if (intent == null) return
        val isAlarmActive = intent.getBooleanExtra("launch_alarm_active", false)
        if (isAlarmActive) {
            val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, 0L)
            val title = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_TITLE) ?: "LUNE Wake-Up"
            val wakeModeStr = intent.getStringExtra(AlarmScheduler.EXTRA_WAKE_MODE) ?: "FOCUS"
            val dismissTypeStr = intent.getStringExtra(AlarmScheduler.EXTRA_DISMISS_TYPE) ?: "MATH"
            val sound = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_SOUND) ?: "Good Morningggg"
            val wallpaper = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_WALLPAPER) ?: "wp_meow_alarm"

            val wakeMode = try { WakeMode.valueOf(wakeModeStr) } catch (e: Exception) { WakeMode.FOCUS }
            val dismissType = try { DismissType.valueOf(dismissTypeStr) } catch (e: Exception) { DismissType.MATH }

            viewModel.triggerAlarmRinging(alarmId, title, wakeMode, dismissType, sound, wallpaper)
        }
    }
}

@Composable
fun LuneApp(
    viewModel: LuneViewModel,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChanged: (ThemeMode) -> Unit = {}
) {
    val alarms by viewModel.alarms.collectAsState()
    val sleepSessions by viewModel.sleepSessions.collectAsState()
    val snoreEvents by viewModel.snoreEvents.collectAsState()
    val alarmEvents by viewModel.alarmEvents.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val activeAlarm by viewModel.activeAlarm.collectAsState()
    val activeSleep by viewModel.activeSleep.collectAsState()
    val googleAuthState by viewModel.googleAuthState.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val isAlarmMuted by viewModel.isAlarmMuted.collectAsState()

    var currentTab by remember { mutableStateOf(LuneTab.ALARMS) }
    var alarmToEdit by remember { mutableStateOf<AlarmEntity?>(null) }
    var isAddEditAlarmOpen by remember { mutableStateOf(false) }
    var isHabitAlarmCreatorOpen by remember { mutableStateOf(false) }
    var forceShowOnboarding by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Android 13+ Notification Permission request
    val context = androidx.compose.ui.platform.LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(activeAlarm.isRinging) {
        if (activeAlarm.isRinging) {
            (context as? MainActivity)?.let { activity ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    activity.setShowWhenLocked(true)
                    activity.setTurnScreenOn(true)
                    val keyguardManager = activity.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
                    keyguardManager?.requestDismissKeyguard(activity, null)
                }
            }
        }
    }

    val lastBlockedMsg by AntiCheatProtectionManager.lastBlockedMessage.collectAsState()
    LaunchedEffect(lastBlockedMsg) {
        lastBlockedMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            AntiCheatProtectionManager.clearBlockedMessage()
        }
    }

    val onAttemptExitBlocked: () -> Unit = {
        AntiCheatProtectionManager.recordPowerOffBlockAttempt(
            context,
            "Mission exit or power-off attempted during active alarm"
        )
        scope.launch {
            snackbarHostState.showSnackbar("🛡️ Prevent Power-off Active! Complete the challenge to exit or switch off.")
        }
        Unit
    }

    // Block back button while alarm is ringing until challenge is completed
    val isAlarmActiveAndUnsolved = activeAlarm.isRinging && !activeAlarm.isConquered
    BackHandler(enabled = isAlarmActiveAndUnsolved) {
        onAttemptExitBlocked()
    }

    val showOnboarding = forceShowOnboarding || (userProfile?.isOnboardingCompleted != true)

    if (showOnboarding) {
        WakeUpMissionPickerScreen(
            currentMission = when (userProfile?.selectedMission) {
                "SHAKE" -> DismissType.SHAKE
                "COLOR_TILES" -> DismissType.COLOR_TILES
                "TYPING" -> DismissType.TYPING
                "OFF" -> DismissType.OFF
                else -> DismissType.MATH
            },
            onMissionConfirmed = { mission ->
                viewModel.completeOnboarding(mission.name)
                forceShowOnboarding = false
                scope.launch {
                    snackbarHostState.showSnackbar("Wake-up mission set to ${mission.title}")
                }
            },
            onDismiss = {
                viewModel.completeOnboarding(userProfile?.selectedMission ?: "MATH")
                forceShowOnboarding = false
            }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Screen content occupies 100% full screen
            Box(modifier = Modifier.fillMaxSize()) {
                when (currentTab) {
                    LuneTab.ALARMS -> {
                        AlarmsScreen(
                            alarms = alarms,
                            onToggleAlarm = { alarm, enabled -> viewModel.toggleAlarm(alarm, enabled) },
                            onEditAlarm = { alarm ->
                                alarmToEdit = alarm
                                isAddEditAlarmOpen = true
                            },
                            onDeleteAlarm = { alarm -> viewModel.deleteAlarm(alarm) },
                            onTestAlarm = { alarm ->
                                viewModel.triggerAlarmRinging(
                                    alarm.id,
                                    alarm.title,
                                    alarm.wakeMode,
                                    alarm.dismissType,
                                    alarm.sound,
                                    alarm.wallpaper
                                )
                            },
                            onAddNewAlarm = {
                                alarmToEdit = null
                                isAddEditAlarmOpen = true
                            },
                            onAddHabitAlarm = {
                                isHabitAlarmCreatorOpen = true
                            },
                            onSaveQuickAlarm = { mins, sound, vol, vib ->
                                viewModel.saveQuickAlarm(mins, sound, vol, vib)
                                scope.launch {
                                    snackbarHostState.showSnackbar("⚡ Quick alarm set for $mins min from now")
                                }
                            }
                        )
                    }
                    LuneTab.SLEEP -> {
                        SleepScreen(
                            activeSleep = activeSleep,
                            sleepSessions = sleepSessions,
                            snoreEvents = snoreEvents,
                            userProfile = userProfile,
                            onStartSleep = { viewModel.startSleepSession() },
                            onFinishSleep = { viewModel.finishSleepSession() },
                            onDeleteSession = { session -> viewModel.deleteSleepSession(session) },
                            onSimulateMovement = { isRestless -> viewModel.simulateSleepMovement(isRestless) },
                            onSimulateOvernight = { viewModel.simulateOvernightTracking() },
                            onViewReport = { session ->
                                viewModel.generateMorningReport("Overnight Rest Session")
                            },
                            onAddHabitAlarm = {
                                isHabitAlarmCreatorOpen = true
                            }
                        )
                    }
                    LuneTab.TASKS -> {
                        TasksScreen(
                            tasks = tasks,
                            userProfile = userProfile,
                            onToggleTask = { task -> viewModel.toggleTask(task) },
                            onAddTask = { title, timePill, iconType -> viewModel.addTask(title, timePill, iconType) },
                            onDeleteTask = { task -> viewModel.deleteTask(task) },
                            onResetTasks = { viewModel.resetAllTasks() }
                        )
                    }
                    LuneTab.SETTINGS -> {
                        SettingsScreen(
                            userProfile = userProfile,
                            googleAuthState = googleAuthState,
                            effectiveGoogleClientId = viewModel.getEffectiveGoogleClientId(),
                            themeMode = themeMode,
                            onThemeModeChanged = onThemeModeChanged,
                            onSignInGoogle = {
                                (context as? Activity)?.let { act ->
                                    viewModel.signInWithGoogle(act) { success, err ->
                                        if (!success && err != null) {
                                            scope.launch { snackbarHostState.showSnackbar(err) }
                                        }
                                    }
                                }
                            },
                            onSignInGoogleWithClientId = { clientId ->
                                (context as? Activity)?.let { act ->
                                    viewModel.signInWithGoogle(act, clientId) { success, err ->
                                        if (!success && err != null) {
                                            scope.launch { snackbarHostState.showSnackbar(err) }
                                        }
                                    }
                                }
                            },
                            onSignInGoogleDemo = { email, name ->
                                viewModel.signInWithGoogleDemo(email, name) {
                                    scope.launch { snackbarHostState.showSnackbar("Signed in as $email") }
                                }
                            },
                            onSignOutGoogle = {
                                viewModel.signOutGoogle {
                                    scope.launch { snackbarHostState.showSnackbar("Signed out of Google account") }
                                }
                            },
                            onClearGoogleAuthError = { viewModel.clearGoogleAuthError() },
                            onUpdateProfile = { updated -> viewModel.updateProfile(updated) },
                            onTestAlarm = {
                                val mode = userProfile?.defaultWakeMode ?: WakeMode.FOCUS
                                viewModel.triggerAlarmRinging(
                                    alarmId = 999L,
                                    title = "LUNE Math Wake Test",
                                    wakeMode = mode,
                                    dismissType = DismissType.MATH
                                )
                            },
                            onTestShakeAlarm = {
                                val mode = userProfile?.defaultWakeMode ?: WakeMode.FOCUS
                                viewModel.triggerAlarmRinging(
                                    alarmId = 998L,
                                    title = "Morning Alarm Preview",
                                    wakeMode = mode,
                                    dismissType = DismissType.SHAKE,
                                    sound = com.example.data.model.RingtoneCatalog.MEME_UTH_JA.name
                                )
                            },
                            onTestColorTilesAlarm = {
                                val mode = userProfile?.defaultWakeMode ?: WakeMode.FOCUS
                                viewModel.triggerAlarmRinging(
                                    alarmId = 997L,
                                    title = "Color Tiles Challenge",
                                    wakeMode = mode,
                                    dismissType = DismissType.COLOR_TILES
                                )
                            },
                            onTestTypingAlarm = {
                                val mode = userProfile?.defaultWakeMode ?: WakeMode.FOCUS
                                viewModel.triggerAlarmRinging(
                                    alarmId = 996L,
                                    title = "Typing Alert Challenge",
                                    wakeMode = mode,
                                    dismissType = DismissType.TYPING
                                )
                            },
                            onTestMorningReport = {
                                viewModel.generateMorningReport("Morning Test")
                            },
                            onRestartOnboarding = {
                                forceShowOnboarding = true
                            }
                        )
                    }
                }

                // Full Screen Create / Edit Alarm Page
                AnimatedVisibility(
                    visible = isAddEditAlarmOpen,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    AlarmEditScreen(
                        initialAlarm = alarmToEdit,
                        onDismiss = {
                            isAddEditAlarmOpen = false
                            alarmToEdit = null
                        },
                        onSave = { savedAlarm ->
                            viewModel.saveAlarm(savedAlarm)
                            isAddEditAlarmOpen = false
                            alarmToEdit = null
                            scope.launch {
                                snackbarHostState.showSnackbar("Alarm saved successfully")
                            }
                        },
                        onDelete = { alarmToDelete ->
                            viewModel.deleteAlarm(alarmToDelete)
                            isAddEditAlarmOpen = false
                            alarmToEdit = null
                            scope.launch {
                                snackbarHostState.showSnackbar("Alarm deleted")
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Full Screen Habit Alarm Creator Page
                AnimatedVisibility(
                    visible = isHabitAlarmCreatorOpen,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    HabitAlarmScreen(
                        onClose = {
                            isHabitAlarmCreatorOpen = false
                        },
                        onSaveHabitAlarm = { habitTitle, hour, minute, mission, sound, wallpaper ->
                            viewModel.completeHabitOnboarding(habitTitle, hour, minute, mission, sound, wallpaper)
                            isHabitAlarmCreatorOpen = false
                            scope.launch {
                                val tag = if (com.example.data.model.RingtoneCatalog.isMemeRingtone(sound)) "👾 " else ""
                                snackbarHostState.showSnackbar("Habit alarm scheduled for $habitTitle with $tag$sound")
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Full-Screen Ringing Mission Overlay with Wallpaper Display
                var isMissionStarted by remember(activeAlarm.startTimestamp) { mutableStateOf(false) }

                AnimatedVisibility(
                    visible = activeAlarm.isRinging,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    if (!isMissionStarted) {
                        WallpaperAlarmRingingScreen(
                            state = activeAlarm,
                            onStartMission = {
                                if (activeAlarm.dismissType == DismissType.OFF) {
                                    viewModel.dismissConqueredAlarm()
                                } else {
                                    isMissionStarted = true
                                }
                            },
                            onAttemptExitBlocked = onAttemptExitBlocked,
                            isMuted = isAlarmMuted,
                            onToggleMute = { viewModel.toggleAlarmMute() },
                            onAttemptSnooze = {
                                viewModel.attemptEmergencySnooze(
                                    onBlocked = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Snooze not available")
                                        }
                                    },
                                    onGranted = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Alarm snoozed for 5 minutes")
                                        }
                                    }
                                )
                            }
                        )
                    } else {
                        when (activeAlarm.dismissType) {
                            DismissType.MATH -> {
                                MathMissionScreen(
                                    onDismissConquered = {
                                        isMissionStarted = false
                                        viewModel.dismissConqueredAlarm()
                                    },
                                    isMuted = isAlarmMuted,
                                    isLockedMission = true,
                                    onAttemptExitBlocked = onAttemptExitBlocked,
                                    onToggleMute = { viewModel.toggleAlarmMute() }
                                )
                            }
                            DismissType.COLOR_TILES -> {
                                ColorTilesMissionScreen(
                                    onDismissConquered = {
                                        isMissionStarted = false
                                        viewModel.dismissConqueredAlarm()
                                    },
                                    isMuted = isAlarmMuted,
                                    isLockedMission = true,
                                    onAttemptExitBlocked = onAttemptExitBlocked,
                                    onToggleMute = { viewModel.toggleAlarmMute() }
                                )
                            }
                            DismissType.TYPING -> {
                                TypingMissionScreen(
                                    onDismissConquered = {
                                        isMissionStarted = false
                                        viewModel.dismissConqueredAlarm()
                                    },
                                    isMuted = isAlarmMuted,
                                    isLockedMission = true,
                                    onAttemptExitBlocked = onAttemptExitBlocked,
                                    onToggleMute = { viewModel.toggleAlarmMute() }
                                )
                            }
                            else -> {
                                ActiveAlarmScreen(
                                    state = activeAlarm,
                                    onDismissConquered = {
                                        isMissionStarted = false
                                        viewModel.dismissConqueredAlarm()
                                    },
                                    onAttemptSnooze = {
                                        viewModel.attemptEmergencySnooze(
                                            onBlocked = {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Snooze not available")
                                                }
                                            },
                                            onGranted = {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Alarm snoozed for 5 minutes")
                                                }
                                            }
                                        )
                                    },
                                    isMuted = isAlarmMuted,
                                    isLockedMission = true,
                                    onAttemptExitBlocked = onAttemptExitBlocked,
                                    onToggleMute = { viewModel.toggleAlarmMute() },
                                    onShake = { viewModel.onShakeRegistered() }
                                )
                            }
                        }
                    }
                }
            }

            // Floating Snackbar Host
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 96.dp)
            )

            // Floating Navigation Bar overlaying smoothly on the full-screen canvas
            val isSleepTrackingActiveOnTab = currentTab == LuneTab.SLEEP && activeSleep.isTracking
            if (!activeAlarm.isRinging && !isHabitAlarmCreatorOpen && !isAddEditAlarmOpen && !isSleepTrackingActiveOnTab) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    LuneBottomNavigation(
                        currentTab = currentTab,
                        onTabSelected = { currentTab = it }
                    )
                }
            }
        }
    }
}
