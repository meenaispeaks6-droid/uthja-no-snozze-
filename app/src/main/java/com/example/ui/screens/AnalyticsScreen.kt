package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.alarm.rememberAppleHaptics
import com.example.data.model.AlarmEventEntity
import com.example.data.model.UserProfileEntity
import com.example.ui.components.AppText
import com.example.ui.components.AppTextTone
import com.example.ui.components.AppTextVariant
import com.example.ui.components.CardTone
import com.example.ui.components.GradientBlobTone
import com.example.ui.components.LuneButton
import com.example.ui.components.LuneButtonSize
import com.example.ui.components.LuneButtonVariant
import com.example.ui.components.LuneCard
import com.example.ui.components.LuneSectionHeader
import com.example.ui.components.WaveBackground
import com.example.ui.theme.LuneColors

@Composable
fun AnalyticsScreen(
    alarmEvents: List<AlarmEventEntity>,
    userProfile: UserProfileEntity?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = rememberAppleHaptics()
    var isNotified by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "stats_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    WaveBackground(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Custom Header with Prominent Coming Soon Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        AppText(
                            text = "ANALYTICS",
                            variant = AppTextVariant.CAPTION,
                            tone = AppTextTone.SECONDARY
                        )
                        AppText(
                            text = "Stats & Insights",
                            variant = AppTextVariant.TITLE,
                            tone = AppTextTone.DEFAULT
                        )
                    }

                    // Coming Soon Pill Badge
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFE8F6F1))
                            .border(1.dp, Color(0xFFC7EDE0), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF2C9C72),
                                modifier = Modifier.size(13.dp)
                            )
                            AppText(
                                text = "Coming Soon",
                                variant = AppTextVariant.CAPTION,
                                tone = AppTextTone.ACCENT
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                AppText(
                    text = "A quiet, comprehensive view into what improves your mornings.",
                    variant = AppTextVariant.BODY,
                    tone = AppTextTone.MUTED
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Hero Card: Stats Spotlight
            item {
                LuneCard(
                    elevated = true,
                    tonal = CardTone.SAGE,
                    blobTone = GradientBlobTone.SAGE,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Ambient Breathing Insights Icon
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(96.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(LuneColors.surfaceSage)
                                .border(1.5.dp, Color(0xFFBFE7D7), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Insights,
                                contentDescription = "Stats icon",
                                tint = Color(0xFF1E7E59),
                                modifier = Modifier.size(46.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        AppText(
                            text = "Advanced Stats are Coming Soon",
                            variant = AppTextVariant.TITLE,
                            tone = AppTextTone.DEFAULT
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        AppText(
                            text = "We are designing rich analytics to chart your circadian rhythms, anti-snooze consistency, mission speed records, and sleep hygiene trends.",
                            variant = AppTextVariant.BODY,
                            tone = AppTextTone.SECONDARY,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Notify Me CTA Button
                        LuneButton(
                            onClick = {
                                haptics.pulseAppleButtonClick()
                                isNotified = !isNotified
                                Toast.makeText(
                                    context,
                                    if (isNotified) "✨ You'll be notified when Stats & Analytics launch!" else "Notification preference updated.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            variant = if (isNotified) LuneButtonVariant.SECONDARY else LuneButtonVariant.PRIMARY,
                            size = LuneButtonSize.MD
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isNotified) Icons.Filled.AutoAwesome else Icons.Filled.NotificationsActive,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                AppText(
                                    text = if (isNotified) "Notifications Enabled" else "Notify Me When Ready",
                                    variant = AppTextVariant.LABEL,
                                    tone = if (isNotified) AppTextTone.ACCENT else AppTextTone.INVERSE
                                )
                            }
                        }
                    }
                }
            }

            // Preview Highlights Section
            item {
                LuneSectionHeader(
                    title = "Upcoming Analytics"
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatsPreviewCard(
                        icon = Icons.Filled.Timeline,
                        iconBg = Color(0xFFE8F6F1),
                        iconColor = Color(0xFF2C9C72),
                        title = "Circadian Rhythm Mapping",
                        description = "Visual charts of your daily biological energy curves, wake consistency, and bedtime habits."
                    )

                    StatsPreviewCard(
                        icon = Icons.Filled.Bolt,
                        iconBg = Color(0xFFFFEDE6),
                        iconColor = Color(0xFFF67453),
                        title = "Anti-Snooze Consistency",
                        description = "Track your wake-up mission speed, difficulty progressions, and unbroken streak days."
                    )

                    StatsPreviewCard(
                        icon = Icons.Filled.WatchLater,
                        iconBg = Color(0xFFEFE8FD),
                        iconColor = Color(0xFF6B4EF9),
                        title = "Weekly Sleep Debt",
                        description = "Identify cumulative fatigue trends and balance optimal recovery hours effortlessly."
                    )

                    StatsPreviewCard(
                        icon = Icons.Filled.EmojiEvents,
                        iconBg = Color(0xFFFFF7DC),
                        iconColor = Color(0xFFD4971A),
                        title = "Milestone Achievements",
                        description = "Earn badges for 7-day zero-snooze streaks, dawn wake-ups, and cognitive mission mastery."
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun StatsPreviewCard(
    icon: ImageVector,
    iconBg: Color,
    iconColor: Color,
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.92f))
            .border(1.dp, LuneColors.border, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconBg)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                AppText(
                    text = title,
                    variant = AppTextVariant.LABEL,
                    tone = AppTextTone.DEFAULT
                )
                Spacer(modifier = Modifier.height(2.dp))
                AppText(
                    text = description,
                    variant = AppTextVariant.CAPTION,
                    tone = AppTextTone.SECONDARY
                )
            }
        }
    }
}
