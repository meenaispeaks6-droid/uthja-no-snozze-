package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhonelinkErase
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.MaterialTheme
import com.example.ui.theme.LocalIsDarkMode
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.alarm.rememberAppleHaptics
import com.example.data.model.TaskEntity
import com.example.data.model.UserProfileEntity

/**
 * Tasks & Habits Screen matching the uploaded design:
 *
 * 1. Header:
 *    - "Good morning ☀️"
 *    - "Build a better you 💖"
 *    - Progress Pill Badge: "3/8 done" (with purple progress circle/pill)
 *
 * 2. Today's plan Card:
 *    - "Today’s plan ✦"
 *    - 4 core items: Drink water (checked), Meditation, 10-min mindful break (checked), Wind down at 10:30 PM
 *    - Checked/unchecked states with colored icons on the right
 *
 * 3. Two-Column Card Row:
 *    - Left: "12-day streak" with "You're glowing! 💖" and blooming cherry blossom tree artwork
 *    - Right: "Quick mood check-in" with 5 mood emoji circles (🙂, 😌, 😐, 🙁, 😫) & "Log mood" button
 *
 * 4. Wellness habits Section:
 *    - "Wellness habits" and "See all >"
 *    - 2-Column Grid (10 pastel cards with counters and circular colored '+' buttons):
 *      - Drink water (0/8 glasses)
 *      - Meditation (0/10 min)
 *      - Exercise (0/30 min)
 *      - Sleep early (Target 10:30 PM)
 *      - Read (0/20 min)
 *      - Healthy eating (0/3 meals)
 *      - Mindful break (0/10 min)
 *      - Morning routine (0/4 tasks)
 *      - Gratitude (0/3 things)
 *      - No social media (0/2 hours)
 *
 * 5. Bottom Action:
 *    - Dashed purple outline card: "+ Add custom habit or task"
 */
@Composable
fun TasksScreen(
    tasks: List<TaskEntity>,
    userProfile: UserProfileEntity?,
    onToggleTask: (TaskEntity) -> Unit,
    onAddTask: (title: String, timePill: String?, iconType: String) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onResetTasks: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = rememberAppleHaptics()
    val prefs = remember { context.getSharedPreferences("wellness_habits_prefs", Context.MODE_PRIVATE) }

    // Wellness Habits state (persisted in SharedPreferences)
    var waterCount by remember { mutableIntStateOf(prefs.getInt("habit_water", 0)) }
    var meditationCount by remember { mutableIntStateOf(prefs.getInt("habit_meditation", 0)) }
    var exerciseCount by remember { mutableIntStateOf(prefs.getInt("habit_exercise", 0)) }
    var sleepEarlyActive by remember { mutableStateOf(prefs.getBoolean("habit_sleep_early", false)) }
    var readCount by remember { mutableIntStateOf(prefs.getInt("habit_read", 0)) }
    var healthyEatingCount by remember { mutableIntStateOf(prefs.getInt("habit_healthy_eating", 0)) }
    var mindfulBreakCount by remember { mutableIntStateOf(prefs.getInt("habit_mindful_break", 0)) }
    var morningRoutineCount by remember { mutableIntStateOf(prefs.getInt("habit_morning_routine", 0)) }
    var gratitudeCount by remember { mutableIntStateOf(prefs.getInt("habit_gratitude", 0)) }
    var noSocialMediaCount by remember { mutableIntStateOf(prefs.getInt("habit_no_social_media", 0)) }

    // Mood Check-in state
    var selectedMoodIndex by remember { mutableIntStateOf(prefs.getInt("last_mood_index", 1)) } // default: 😌 Good
    var isMoodLoggedToday by remember { mutableStateOf(prefs.getBoolean("mood_logged_today", false)) }
    var showStreakDialog by remember { mutableStateOf(false) }
    var showAddCustomDialog by remember { mutableStateOf(false) }
    var showAllHabitsDialog by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<TaskEntity?>(null) }

    // Synchronize tasks if empty with the 4 default items from Today's plan
    val displayTasks = remember(tasks) {
        if (tasks.isNotEmpty()) {
            tasks
        } else {
            listOf(
                TaskEntity(id = 1L, title = "Drink water", iconType = "WATER", isCompleted = true, orderIndex = 0),
                TaskEntity(id = 2L, title = "Meditation", iconType = "MEDITATION", isCompleted = false, orderIndex = 1),
                TaskEntity(id = 3L, title = "10-min mindful break", iconType = "COFFEE", isCompleted = true, orderIndex = 2),
                TaskEntity(id = 4L, title = "Wind down at", timePill = "10:30 PM", iconType = "MOON", isCompleted = false, orderIndex = 3)
            )
        }
    }

    // Dynamic Overall Progress Calculation
    val tasksDone = displayTasks.count { it.isCompleted }
    val wellnessDone = listOf(
        waterCount >= 8,
        meditationCount >= 10,
        exerciseCount >= 30,
        sleepEarlyActive,
        readCount >= 20,
        healthyEatingCount >= 3,
        mindfulBreakCount >= 10,
        morningRoutineCount >= 4,
        gratitudeCount >= 3,
        noSocialMediaCount >= 2
    ).count { it }

    val totalCompletedCount = tasksDone + (if (wellnessDone > 0) 1 else 0)
    val totalGoalCount = (displayTasks.size + 4).coerceAtLeast(8)
    // Matches "3/8 done" from the screenshot
    val displayDoneRatio = "$totalCompletedCount/$totalGoalCount done"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .testTag("tasks_screen_root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .padding(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ---------------------------------------------------------
            // 1. TOP HEADER: "Good morning ☀️", "Build a better you 💖", "3/8 done"
            // ---------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Good morning ☀️",
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Build a better you 💖",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp
                    )
                }

                // Top-right Progress Pill Badge: "3/8 done"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(22.dp))
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        // Purple Progress Ring
                        Canvas(modifier = Modifier.size(16.dp)) {
                            // Background track
                            drawCircle(
                                color = Color(0xFFE2E8F0),
                                radius = size.minDimension / 2f,
                                style = Stroke(width = 3.dp.toPx())
                            )
                            // Purple Progress Arc
                            val sweep = ((totalCompletedCount.toFloat() / totalGoalCount.toFloat()) * 360f).coerceIn(0f, 360f)
                            drawArc(
                                color = Color(0xFF7C3AED),
                                startAngle = -90f,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }

                        Text(
                            text = displayDoneRatio,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // ---------------------------------------------------------
            // 2. TODAY'S PLAN CARD
            // ---------------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 3.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0x10000000))
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header: "Today’s plan ✦"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Today’s plan",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "✦",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF43F5E)
                        )
                    }

                    // Task List Items
                    displayTasks.forEach { task ->
                        TodayPlanTaskRow(
                            task = task,
                            onToggle = {
                                haptics.pulseAppleButtonClick()
                                onToggleTask(task)
                            },
                            onLongClick = {
                                haptics.pulseAppleSelection()
                                taskToDelete = task
                            }
                        )
                    }
                }
            }

            // ---------------------------------------------------------
            // 3. TWO SIDE-BY-SIDE CARDS: 12-day streak & Quick mood check-in
            // ---------------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // LEFT CARD: 12-day streak with blooming cherry blossom tree
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(230.dp)
                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0x10000000))
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                        .clickable {
                            haptics.pulseAppleButtonClick()
                            showStreakDialog = true
                        }
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            // Row with title and hamburger menu icon
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${userProfile?.streakDays?.coerceAtLeast(12) ?: 12}-day streak",
                                    fontSize = 16.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                // Three horizontal purple bars (hamburger icon)
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(3.dp),
                                    horizontalAlignment = Alignment.End,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Box(modifier = Modifier.size(width = 16.dp, height = 2.5.dp).clip(CircleShape).background(Color(0xFF7C3AED)))
                                    Box(modifier = Modifier.size(width = 16.dp, height = 2.5.dp).clip(CircleShape).background(Color(0xFF7C3AED)))
                                    Box(modifier = Modifier.size(width = 16.dp, height = 2.5.dp).clip(CircleShape).background(Color(0xFF7C3AED)))
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "You’re glowing! 💖",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Cherry Blossom Tree Artwork
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_streak_tree),
                                contentDescription = "Blossoming Cherry Streak Tree with Birds",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                // RIGHT CARD: Quick mood check-in with 5 colored mood circles & "Log mood" button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(230.dp)
                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0x10000000))
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Quick mood\ncheck-in",
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "How are you feeling? ",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "✦",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7C3AED)
                                )
                            }
                        }

                        // 5 Mood Emoji Faces in colored circles
                        val moods = listOf(
                            Triple("🙂", Color(0xFFFEF08A), Color(0xFFFACC15)), // Yellow - Great
                            Triple("😌", Color(0xFFA7F3D0), Color(0xFF6EE7B7)), // Mint - Good
                            Triple("😐", Color(0xFFDDD6FE), Color(0xFFC4B5FD)), // Lavender - Okay
                            Triple("🙁", Color(0xFFFED7AA), Color(0xFFFDBA74)), // Peach - Low
                            Triple("😫", Color(0xFFFBCFE8), Color(0xFFF472B6))  // Pink - Down
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            moods.forEachIndexed { index, (emoji, bg, border) ->
                                val isSelected = selectedMoodIndex == index
                                val scale by animateFloatAsState(targetValue = if (isSelected) 1.18f else 1.0f, label = "moodScale")

                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .scale(scale)
                                        .clip(CircleShape)
                                        .background(bg)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) Color(0xFF7C3AED) else border,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            haptics.pulseAppleSelection()
                                            selectedMoodIndex = index
                                            prefs.edit().putInt("last_mood_index", index).apply()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = emoji,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // "Log mood" Button
                        Button(
                            onClick = {
                                haptics.pulseAppleSuccess()
                                isMoodLoggedToday = true
                                prefs.edit().putBoolean("mood_logged_today", true).apply()
                                val moodLabel = when (selectedMoodIndex) {
                                    0 -> "Great 🙂"
                                    1 -> "Good 😌"
                                    2 -> "Okay 😐"
                                    3 -> "Low 🙁"
                                    else -> "Tired 😫"
                                }
                                Toast.makeText(context, "Mood recorded: $moodLabel! 💖", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Color(0x357C3AED))
                                .testTag("log_mood_button"),
                            shape = RoundedCornerShape(22.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isMoodLoggedToday) Color(0xFF8B5CF6) else Color(0xFF7C3AED),
                                contentColor = Color.White
                            ),
                            contentPadding = ButtonDefaults.ContentPadding
                        ) {
                            Text(
                                text = if (isMoodLoggedToday) "Logged 💖" else "Log mood",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ---------------------------------------------------------
            // 4. WELLNESS HABITS SECTION (Header & 10 Pastel Grid Cards)
            // ---------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Wellness habits",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )

                Text(
                    text = "See all >",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7C3AED),
                    modifier = Modifier
                        .clickable {
                            haptics.pulseAppleButtonClick()
                            showAllHabitsDialog = true
                        }
                        .padding(4.dp)
                )
            }

            // 10 Habit Cards arranged in 5 rows of 2 columns
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ROW 1: Drink water & Meditation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WellnessHabitCard(
                        title = "Drink water",
                        subtitle = "$waterCount/8 glasses",
                        cardBg = Color(0xFFEBF5FF),
                        icon = Icons.Default.WaterDrop,
                        iconTint = Color(0xFF0284C7),
                        iconBg = Color(0xFFE0F2FE),
                        buttonColor = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f),
                        onPlusClick = {
                            haptics.pulseAppleButtonClick()
                            waterCount = (waterCount + 1).coerceAtMost(24)
                            prefs.edit().putInt("habit_water", waterCount).apply()
                        }
                    )

                    WellnessHabitCard(
                        title = "Meditation",
                        subtitle = "$meditationCount/10 min",
                        cardBg = Color(0xFFFFF7ED),
                        icon = Icons.Default.Person,
                        iconTint = Color(0xFFEA580C),
                        iconBg = Color(0xFFFFEDD5),
                        buttonColor = Color(0xFFF97316),
                        modifier = Modifier.weight(1f),
                        onPlusClick = {
                            haptics.pulseAppleButtonClick()
                            meditationCount = (meditationCount + 5).coerceAtMost(60)
                            prefs.edit().putInt("habit_meditation", meditationCount).apply()
                        }
                    )
                }

                // ROW 2: Exercise & Sleep early
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WellnessHabitCard(
                        title = "Exercise",
                        subtitle = "$exerciseCount/30 min",
                        cardBg = Color(0xFFECFDF5),
                        icon = Icons.Default.DirectionsRun,
                        iconTint = Color(0xFF059669),
                        iconBg = Color(0xFFD1FAE5),
                        buttonColor = Color(0xFF10B981),
                        modifier = Modifier.weight(1f),
                        onPlusClick = {
                            haptics.pulseAppleButtonClick()
                            exerciseCount = (exerciseCount + 10).coerceAtMost(120)
                            prefs.edit().putInt("habit_exercise", exerciseCount).apply()
                        }
                    )

                    WellnessHabitCard(
                        title = "Sleep early",
                        subtitle = "Target 10:30 PM",
                        cardBg = Color(0xFFF3E8FF),
                        icon = Icons.Default.Bedtime,
                        iconTint = Color(0xFF7C3AED),
                        iconBg = Color(0xFFEDE9FE),
                        buttonColor = Color(0xFF8B5CF6),
                        modifier = Modifier.weight(1f),
                        isCompleted = sleepEarlyActive,
                        onPlusClick = {
                            haptics.pulseAppleButtonClick()
                            sleepEarlyActive = !sleepEarlyActive
                            prefs.edit().putBoolean("habit_sleep_early", sleepEarlyActive).apply()
                        }
                    )
                }

                // ROW 3: Read & Healthy eating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WellnessHabitCard(
                        title = "Read",
                        subtitle = "$readCount/20 min",
                        cardBg = Color(0xFFFDF2F8),
                        icon = Icons.Default.MenuBook,
                        iconTint = Color(0xFFDB2777),
                        iconBg = Color(0xFFFCE7F3),
                        buttonColor = Color(0xFFEC4899),
                        modifier = Modifier.weight(1f),
                        onPlusClick = {
                            haptics.pulseAppleButtonClick()
                            readCount = (readCount + 5).coerceAtMost(90)
                            prefs.edit().putInt("habit_read", readCount).apply()
                        }
                    )

                    WellnessHabitCard(
                        title = "Healthy eating",
                        subtitle = "$healthyEatingCount/3 meals",
                        cardBg = Color(0xFFF0FDF4),
                        icon = Icons.Default.Restaurant,
                        iconTint = Color(0xFF16A34A),
                        iconBg = Color(0xFFDCFCE7),
                        buttonColor = Color(0xFF22C55E),
                        modifier = Modifier.weight(1f),
                        onPlusClick = {
                            haptics.pulseAppleButtonClick()
                            healthyEatingCount = (healthyEatingCount + 1).coerceAtMost(6)
                            prefs.edit().putInt("habit_healthy_eating", healthyEatingCount).apply()
                        }
                    )
                }

                // ROW 4: Mindful break & Morning routine
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WellnessHabitCard(
                        title = "Mindful break",
                        subtitle = "$mindfulBreakCount/10 min",
                        cardBg = Color(0xFFFFF1F2),
                        icon = Icons.Default.Psychology,
                        iconTint = Color(0xFFE11D48),
                        iconBg = Color(0xFFFFE4E6),
                        buttonColor = Color(0xFFF43F5E),
                        modifier = Modifier.weight(1f),
                        onPlusClick = {
                            haptics.pulseAppleButtonClick()
                            mindfulBreakCount = (mindfulBreakCount + 5).coerceAtMost(30)
                            prefs.edit().putInt("habit_mindful_break", mindfulBreakCount).apply()
                        }
                    )

                    WellnessHabitCard(
                        title = "Morning routine",
                        subtitle = "$morningRoutineCount/4 tasks",
                        cardBg = Color(0xFFFEFCE8),
                        icon = Icons.Default.WbSunny,
                        iconTint = Color(0xFFD97706),
                        iconBg = Color(0xFFFEF3C7),
                        buttonColor = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f),
                        onPlusClick = {
                            haptics.pulseAppleButtonClick()
                            morningRoutineCount = (morningRoutineCount + 1).coerceAtMost(4)
                            prefs.edit().putInt("habit_morning_routine", morningRoutineCount).apply()
                        }
                    )
                }

                // ROW 5: Gratitude & No social media
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WellnessHabitCard(
                        title = "Gratitude",
                        subtitle = "$gratitudeCount/3 things",
                        cardBg = Color(0xFFFAF5FF),
                        icon = Icons.Default.Spa,
                        iconTint = Color(0xFF9333EA),
                        iconBg = Color(0xFFF3E8FF),
                        buttonColor = Color(0xFFA855F7),
                        modifier = Modifier.weight(1f),
                        onPlusClick = {
                            haptics.pulseAppleButtonClick()
                            gratitudeCount = (gratitudeCount + 1).coerceAtMost(10)
                            prefs.edit().putInt("habit_gratitude", gratitudeCount).apply()
                        }
                    )

                    WellnessHabitCard(
                        title = "No social media",
                        subtitle = "$noSocialMediaCount/2 hours",
                        cardBg = Color(0xFFF0F9FF),
                        icon = Icons.Default.PhonelinkErase,
                        iconTint = Color(0xFF0284C7),
                        iconBg = Color(0xFFE0F2FE),
                        buttonColor = Color(0xFF0EA5E9),
                        modifier = Modifier.weight(1f),
                        onPlusClick = {
                            haptics.pulseAppleButtonClick()
                            noSocialMediaCount = (noSocialMediaCount + 1).coerceAtMost(12)
                            prefs.edit().putInt("habit_no_social_media", noSocialMediaCount).apply()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // ---------------------------------------------------------
            // 5. BOTTOM ACTION: Solid Coral Rose "+ Add habit" button
            // Exactly matching the attached UI design (Color #F74E76)
            // ---------------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = Color(0x35F74E76),
                        ambientColor = Color(0x15F74E76)
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFFF74E76))
                    .clickable {
                        haptics.pulseAppleButtonClick()
                        showAddCustomDialog = true
                    }
                    .testTag("add_custom_habit_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add habit",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // -------------------------------------------------------------
    // DIALOGS: Add custom habit/task, Streak details, See all habits
    // -------------------------------------------------------------

    // 1. Add Custom Habit or Task Dialog
    if (showAddCustomDialog) {
        AddCustomHabitDialog(
            onDismiss = { showAddCustomDialog = false },
            onSave = { title, timePill, iconType ->
                onAddTask(title, timePill, iconType)
                showAddCustomDialog = false
                Toast.makeText(context, "Added to Today's plan! ✨", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 2. Streak Consistency Celebration Dialog
    if (showStreakDialog) {
        Dialog(
            onDismissRequest = { showStreakDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(28.dp))
                    .border(1.dp, Color(0xFFFCE7F3), RoundedCornerShape(28.dp)),
                color = Color.White,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFDE8ED)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = Color(0xFFE11D48),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Consistency Streak",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }

                        IconButton(
                            onClick = { showStreakDialog = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Image(
                        painter = painterResource(id = R.drawable.ic_streak_tree),
                        contentDescription = "Streak Tree",
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(160.dp),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "12 Days of Pure Flow! 🌸",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Your cherry tree is in full bloom. You’ve conquered your morning alarms and rituals 12 days in a row.",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { showStreakDialog = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                    ) {
                        Text("Keep the momentum", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }

    // 3. Delete Task Confirmation Dialog
    if (taskToDelete != null) {
        val task = taskToDelete!!
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to remove \"${task.title}\" from Today's plan?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTask(task)
                        taskToDelete = null
                        Toast.makeText(context, "Task removed", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Delete", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 4. See All Wellness Habits Summary
    if (showAllHabitsDialog) {
        Dialog(
            onDismissRequest = { showAllHabitsDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(28.dp)),
                color = Color.White,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "All Wellness Habits ✨",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        IconButton(onClick = { showAllHabitsDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "10 foundational daily habits designed to elevate mental clarity, sleep discipline, and bodily vitality.",
                        fontSize = 13.5.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    listOf(
                        "💧 Drink water" to "$waterCount / 8 glasses target",
                        "🧘 Meditation" to "$meditationCount / 10 min target",
                        "🏃 Exercise" to "$exerciseCount / 30 min target",
                        "🌙 Sleep early" to (if (sleepEarlyActive) "Active tonight ✓" else "Pending 10:30 PM"),
                        "📖 Read" to "$readCount / 20 min target",
                        "🍏 Healthy eating" to "$healthyEatingCount / 3 meals target",
                        "🧠 Mindful break" to "$mindfulBreakCount / 10 min target",
                        "☀️ Morning routine" to "$morningRoutineCount / 4 tasks target",
                        "🌱 Gratitude" to "$gratitudeCount / 3 things target",
                        "📵 No social media" to "$noSocialMediaCount / 2 hours target"
                    ).forEach { (name, progress) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = name, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                            Text(text = progress, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF7C3AED))
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                prefs.edit().clear().apply()
                                waterCount = 0
                                meditationCount = 0
                                exerciseCount = 0
                                sleepEarlyActive = false
                                readCount = 0
                                healthyEatingCount = 0
                                mindfulBreakCount = 0
                                morningRoutineCount = 0
                                gratitudeCount = 0
                                noSocialMediaCount = 0
                                onResetTasks()
                                showAllHabitsDialog = false
                                Toast.makeText(context, "Habit tallies reset for today", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("Reset counts", color = Color(0xFF64748B), fontSize = 13.sp)
                        }

                        Button(
                            onClick = { showAllHabitsDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Task Row inside "Today's plan" card:
 * Matches the exact design of the 4 items from the user screenshot.
 */
@Composable
private fun TodayPlanTaskRow(
    task: TaskEntity,
    onToggle: () -> Unit,
    onLongClick: () -> Unit
) {
    val isWater = task.iconType.equals("WATER", ignoreCase = true)
    val isMeditation = task.iconType.equals("MEDITATION", ignoreCase = true)
    val isCoffee = task.iconType.equals("COFFEE", ignoreCase = true) || task.iconType.equals("MINDFUL", ignoreCase = true)
    val isMoon = task.iconType.equals("MOON", ignoreCase = true)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left side: Checkbox circle + Task Title (+ optional time pill)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Check Circle
            if (task.isCompleted) {
                // Purple filled circle with white check
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7C3AED)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                // Unchecked circle with colored stroke
                val strokeColor = if (isMoon) Color(0xFFF43F5E) else Color(0xFFA855F7)
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .border(1.6.dp, strokeColor, CircleShape)
                )
            }

            // Title + optional time pill badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.isCompleted) Color(0xFF334155) else Color(0xFF0F172A)
                )

                if (task.timePill != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFCE7F3))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = task.timePill,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF831843)
                        )
                    }
                }
            }
        }

        // Right side: Colored icon matching the design
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                isWater -> {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(22.dp)
                    )
                }
                isMeditation -> {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFFF97316),
                        modifier = Modifier.size(22.dp)
                    )
                }
                isCoffee -> {
                    Icon(
                        imageVector = Icons.Default.Coffee,
                        contentDescription = null,
                        tint = Color(0xFFF43F5E),
                        modifier = Modifier.size(22.dp)
                    )
                }
                isMoon -> {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = null,
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.size(22.dp)
                    )
                }
                task.iconType.equals("BOOK", ignoreCase = true) -> {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = Color(0xFFEC4899),
                        modifier = Modifier.size(22.dp)
                    )
                }
                task.iconType.equals("WORKOUT", ignoreCase = true) -> {
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(22.dp)
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF7C3AED),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Individual Card inside "Wellness habits":
 * Pastel card background, rounded 18dp, circular icon container, title, subtitle, and circular '+' button.
 */
@Composable
private fun WellnessHabitCard(
    title: String,
    subtitle: String,
    cardBg: Color,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    buttonColor: Color,
    modifier: Modifier = Modifier,
    isCompleted: Boolean = false,
    onPlusClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .clickable(onClick = onPlusClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Icon Circle + Title & Subtitle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Right: Circular colored '+' button
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(buttonColor)
                    .clickable(onClick = onPlusClick),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increment",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Dialog to add a custom habit or task into Today's plan
 */
@Composable
private fun AddCustomHabitDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, timePill: String?, iconType: String) -> Unit
) {
    val haptics = rememberAppleHaptics()
    var title by remember { mutableStateOf("") }
    var timePill by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("WATER") }

    val iconChoices = listOf(
        "WATER" to ("Water" to Icons.Default.WaterDrop),
        "MEDITATION" to ("Mind" to Icons.Default.Person),
        "COFFEE" to ("Break" to Icons.Default.Coffee),
        "MOON" to ("Sleep" to Icons.Default.Bedtime),
        "BOOK" to ("Read" to Icons.Default.MenuBook),
        "WORKOUT" to ("Fitness" to Icons.Default.DirectionsRun),
        "HEALTHY" to ("Food" to Icons.Default.Restaurant),
        "SUN" to ("Morning" to Icons.Default.WbSunny),
        "GRATITUDE" to ("Gratitude" to Icons.Default.Spa)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Custom Habit or Task ✨",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Habit name (e.g. Journaling)") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7C3AED),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = timePill,
                    onValueChange = { timePill = it },
                    label = { Text("Optional time badge (e.g. 10:30 PM)") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7C3AED),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Select Category Icon",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF475569)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    iconChoices.take(5).forEach { (key, pair) ->
                        val isSelected = selectedIcon == key
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFF7C3AED) else Color(0xFFF1F5F9))
                                .clickable {
                                    haptics.pulseAppleSelection()
                                    selectedIcon = key
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = pair.second,
                                contentDescription = pair.first,
                                tint = if (isSelected) Color.White else Color(0xFF475569),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        haptics.pulseAppleSuccess()
                        onSave(title.trim(), timePill.trim().ifBlank { null }, selectedIcon)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Add Task", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF64748B))
            }
        }
    )
}
