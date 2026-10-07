package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm.AlarmScheduler
import com.example.data.model.AlarmEntity
import com.example.ui.theme.LuneColors
import java.util.concurrent.TimeUnit

private val dayOrder = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

private fun isDayActive(day: String, repeatDays: String): Boolean {
    val upper = repeatDays.uppercase()
    if (upper == "DAILY" || upper == "EVERY DAY") return true
    if (upper == "WEEKDAYS" && (day in listOf("Mon", "Tue", "Wed", "Thu", "Fri"))) return true
    if (upper == "WEEKENDS" && (day in listOf("Sat", "Sun"))) return true
    val token = day.uppercase()
    return upper.contains(token)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LuneAlarmItem(
    alarm: AlarmEntity,
    modifier: Modifier = Modifier,
    onToggle: ((Boolean) -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onTest: (() -> Unit)? = null
) {
    val displayHour = if (alarm.hour == 0) 12 else if (alarm.hour > 12) alarm.hour - 12 else alarm.hour
    val period = if (alarm.hour < 12) "AM" else "PM"
    val timeFormatted = String.format("%02d:%02d", displayHour, alarm.minute)

    val nextTrigger = AlarmScheduler.calculateNextTriggerTime(alarm.hour, alarm.minute, alarm.repeatDays)
    val msUntil = maxOf(0L, nextTrigger - System.currentTimeMillis())
    val hours = TimeUnit.MILLISECONDS.toHours(msUntil)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(msUntil) % 60
    val countdown = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

    val isCardEnabled = alarm.isEnabled

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (isCardEnabled) Color(0xF7FFFFFF) else LuneColors.surfaceSoft,
        border = BorderStroke(
            1.dp,
            if (isCardEnabled) LuneColors.borderGlow else LuneColors.border
        ),
        shadowElevation = if (isCardEnabled) 5.dp else 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .then(if (!isCardEnabled) Modifier.alpha(0.68f) else Modifier)
            .testTag("alarm_item_${alarm.id}")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Time, AM/PM + Countdown, and Custom Toggle
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    AppText(
                        text = timeFormatted,
                        variant = AppTextVariant.DISPLAY,
                        tone = AppTextTone.DEFAULT
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    AppText(
                        text = "$period • in $countdown",
                        variant = AppTextVariant.LABEL,
                        tone = AppTextTone.ACCENT
                    )
                }

                LuneToggle(
                    value = alarm.isEnabled,
                    onValueChange = onToggle,
                    modifier = Modifier.testTag("alarm_switch_${alarm.id}")
                )
            }

            // Badges Row
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val hasRepeat = alarm.repeatDays.isNotEmpty() && alarm.repeatDays != "ONCE"
                Badge(
                    label = if (hasRepeat) alarm.repeatDays else "Once",
                    active = hasRepeat
                )
                Badge(
                    label = alarm.wakeMode.title,
                    active = true
                )
                Badge(
                    label = "${alarm.dismissType.title} challenge",
                    active = true
                )
                val isMeme = com.example.data.model.RingtoneCatalog.isMemeRingtone(alarm.sound)
                Badge(
                    label = if (isMeme) "👾 Meme: ${alarm.sound.take(15)}" else "🎵 ${alarm.sound}",
                    active = isMeme
                )
            }

            // Bottom Row: Alarm Title Label, 7 Days row, and Edit/Delete/Test Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    AppText(
                        text = alarm.title,
                        variant = AppTextVariant.BODY_SMALL,
                        tone = AppTextTone.SECONDARY
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // 7 Days Pill Row
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        dayOrder.forEach { day ->
                            val active = isDayActive(day, alarm.repeatDays)
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (active) LuneColors.primary else LuneColors.surfaceMuted)
                                    .border(
                                        1.dp,
                                        if (active) LuneColors.primaryDeep else LuneColors.border,
                                        CircleShape
                                    )
                                    .widthIn(min = 36.dp)
                                    .padding(horizontal = 7.dp, vertical = 5.dp)
                            ) {
                                AppText(
                                    text = day,
                                    variant = AppTextVariant.CAPTION,
                                    tone = if (active) AppTextTone.INVERSE else AppTextTone.MUTED,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                // Action Buttons: Test, Edit, Delete
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    if (onTest != null) {
                        IconButtonCircle(
                            backgroundColor = LuneColors.surfaceGold,
                            onClick = onTest,
                            contentDescription = "Test Alarm"
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Test",
                                tint = LuneColors.primaryDeep,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (onEdit != null) {
                        IconButtonCircle(
                            backgroundColor = LuneColors.surfaceLavender,
                            onClick = onEdit,
                            contentDescription = "Edit Alarm"
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit",
                                tint = LuneColors.primaryDeep,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (onDelete != null) {
                        IconButtonCircle(
                            backgroundColor = LuneColors.surfacePeach,
                            onClick = onDelete,
                            contentDescription = "Delete Alarm"
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = LuneColors.coral,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Badge(
    label: String,
    active: Boolean = false
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(CircleShape)
            .background(if (active) LuneColors.surfaceLavender else LuneColors.surfaceSoft)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        AppText(
            text = label,
            variant = AppTextVariant.CAPTION,
            tone = if (active) AppTextTone.ACCENT else AppTextTone.SECONDARY
        )
    }
}

@Composable
private fun IconButtonCircle(
    backgroundColor: Color,
    onClick: () -> Unit,
    contentDescription: String,
    content: @Composable () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick)
    ) {
        content()
    }
}
