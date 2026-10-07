package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.SleepSessionEntity
import com.example.ui.theme.LuneColors

data class MorningSleepReportData(
    val session: SleepSessionEntity,
    val alarmTitle: String = "Morning Alarm",
    val isFreshlyRecorded: Boolean = true
)

@Composable
fun MorningSleepReportDialog(
    report: MorningSleepReportData,
    onDismiss: () -> Unit
) {
    val session = report.session
    val hours = session.durationMinutes / 60
    val mins = session.durationMinutes % 60
    val durationText = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

    val deepHours = session.deepSleepMinutes / 60
    val deepMins = session.deepSleepMinutes % 60
    val deepText = "${deepHours}h ${deepMins}m"

    val lightHours = session.lightSleepMinutes / 60
    val lightMins = session.lightSleepMinutes % 60
    val lightText = "${lightHours}h ${lightMins}m"

    val restlessMins = session.restlessMinutes

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(28.dp))
                .testTag("morning_sleep_report_dialog"),
            color = LuneColors.surface,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Sun Icon & Morning Title
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(LuneColors.surfacePeach)
                ) {
                    Icon(
                        imageVector = Icons.Filled.WbSunny,
                        contentDescription = "Morning Sun",
                        tint = LuneColors.primaryDeep,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                AppText(
                    text = "MORNING REPORT",
                    variant = AppTextVariant.EYEBROW,
                    tone = AppTextTone.ACCENT
                )
                AppText(
                    text = "Wake-Up & Sleep Summary",
                    variant = AppTextVariant.TITLE,
                    tone = AppTextTone.DEFAULT
                )
                AppText(
                    text = "Alarm conquered • Movement patterns analyzed",
                    variant = AppTextVariant.BODY_SMALL,
                    tone = AppTextTone.SECONDARY
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Score Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = LuneColors.surfaceLavender),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            AppText(
                                text = "OVERNIGHT QUALITY",
                                variant = AppTextVariant.LABEL,
                                tone = AppTextTone.ACCENT
                            )
                            AppText(
                                text = "${session.sleepScore}%",
                                variant = AppTextVariant.HERO,
                                tone = AppTextTone.DEFAULT
                            )
                            AppText(
                                text = session.movementRating,
                                variant = AppTextVariant.BODY_SMALL,
                                tone = AppTextTone.SECONDARY
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            AppText(
                                text = "DURATION",
                                variant = AppTextVariant.LABEL,
                                tone = AppTextTone.MUTED
                            )
                            AppText(
                                text = durationText,
                                variant = AppTextVariant.TITLE,
                                tone = AppTextTone.DEFAULT
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Goal Met",
                                    tint = if (session.sleepGoalMet) LuneColors.primaryDeep else LuneColors.textMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                AppText(
                                    text = if (session.sleepGoalMet) "Goal Reached" else "Under Target",
                                    variant = AppTextVariant.CAPTION,
                                    tone = if (session.sleepGoalMet) AppTextTone.ACCENT else AppTextTone.MUTED
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Accelerometer Movement Section
                LuneCard(
                    tonal = CardTone.DEFAULT,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Sensors,
                                contentDescription = "Accelerometer",
                                tint = LuneColors.primaryDeep,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            AppText(
                                text = "Accelerometer Motion Tracking",
                                variant = AppTextVariant.LABEL,
                                tone = AppTextTone.DEFAULT
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                AppText(
                                    text = "Total Movements",
                                    variant = AppTextVariant.CAPTION,
                                    tone = AppTextTone.MUTED
                                )
                                AppText(
                                    text = "${session.movementCount} shifts",
                                    variant = AppTextVariant.BODY_LARGE,
                                    tone = AppTextTone.DEFAULT
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                AppText(
                                    text = "Restlessness Rating",
                                    variant = AppTextVariant.CAPTION,
                                    tone = AppTextTone.MUTED
                                )
                                AppText(
                                    text = session.movementRating,
                                    variant = AppTextVariant.BODY_LARGE,
                                    tone = AppTextTone.ACCENT
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Visual Motion Distribution Bar
                        AppText(
                            text = "Sleep Stage Estimation",
                            variant = AppTextVariant.CAPTION,
                            tone = AppTextTone.MUTED
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val totalMins = session.durationMinutes.coerceAtLeast(1).toFloat()
                        val deepRatio = (session.deepSleepMinutes / totalMins).coerceIn(0.1f, 0.8f)
                        val lightRatio = (session.lightSleepMinutes / totalMins).coerceIn(0.1f, 0.8f)
                        val restlessRatio = (1f - deepRatio - lightRatio).coerceIn(0.05f, 0.5f)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(deepRatio)
                                    .background(LuneColors.primaryDeep)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(lightRatio)
                                    .background(LuneColors.accentLavender)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(restlessRatio)
                                    .background(LuneColors.coral)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(LuneColors.primaryDeep)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                AppText(
                                    text = "Deep ($deepText)",
                                    variant = AppTextVariant.CAPTION,
                                    tone = AppTextTone.SECONDARY
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(LuneColors.accentLavender)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                AppText(
                                    text = "Light ($lightText)",
                                    variant = AppTextVariant.CAPTION,
                                    tone = AppTextTone.SECONDARY
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(LuneColors.coral)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                AppText(
                                    text = "Restless (${restlessMins}m)",
                                    variant = AppTextVariant.CAPTION,
                                    tone = AppTextTone.DANGER
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Dismiss / Confirm Button
                LuneButton(
                    onClick = onDismiss,
                    variant = LuneButtonVariant.PRIMARY,
                    size = LuneButtonSize.LG,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dismiss_morning_report_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.WbSunny,
                        contentDescription = "Start Day",
                        tint = LuneColors.surface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AppText(
                        text = "Great Rest • Start My Day",
                        variant = AppTextVariant.LABEL,
                        tone = AppTextTone.INVERSE
                    )
                }
            }
        }
    }
}
