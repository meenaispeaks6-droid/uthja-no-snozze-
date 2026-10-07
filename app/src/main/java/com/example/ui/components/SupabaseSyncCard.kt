package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SupabaseUiState
import com.example.ui.theme.LuneColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SupabaseSyncCard(
    state: SupabaseUiState,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onOpenAuthDialog: () -> Unit,
    onSignOut: () -> Unit,
    onClearMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSetupGuide by remember { mutableStateOf(false) }

    LuneCard(
        elevated = true,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header: Supabase Logo & Status Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF3ECF8E).copy(alpha = 0.15f))
                            .border(1.5.dp, Color(0xFF3ECF8E), RoundedCornerShape(10.dp))
                    ) {
                        Text(
                            text = "⚡",
                            fontSize = 18.sp
                        )
                    }

                    Column {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppText(
                                text = "Supabase Cloud",
                                variant = AppTextVariant.TITLE_SMALL
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF3ECF8E).copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Postgres + Auth",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E7E52)
                                )
                            }
                        }
                        AppText(
                            text = if (state.isConfigured) "Realtime Sync & Cloud Backup" else "Database & Auth Integration",
                            variant = AppTextVariant.BODY_SMALL,
                            tone = AppTextTone.SECONDARY
                        )
                    }
                }

                // Connection badge
                val badgeColor = if (state.isConfigured) Color(0xFF3ECF8E) else Color(0xFFFFB2C5)
                val badgeBg = if (state.isConfigured) Color(0xFFE8F9F1) else Color(0xFFFFF0F5)
                val badgeText = if (state.isConfigured) "Connected" else "Setup Needed"

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeBg)
                        .border(1.dp, badgeColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(badgeColor)
                    )
                    Text(
                        text = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (state.isConfigured) Color(0xFF1B6B45) else Color(0xFF9E2A4B)
                    )
                }
            }

            // User session row or status callout
            if (state.isConfigured) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF9F7FD))
                        .border(1.dp, LuneColors.border, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF3ECF8E),
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                AppText(
                                    text = if (state.userEmail != null) state.userEmail else "Guest Mode (Anonymous)",
                                    variant = AppTextVariant.BODY
                                )
                                if (state.lastSyncTime != null) {
                                    val timeStr = SimpleDateFormat("h:mm a, MMM d", Locale.getDefault()).format(Date(state.lastSyncTime))
                                    AppText(
                                        text = "Last sync: $timeStr",
                                        variant = AppTextVariant.CAPTION,
                                        tone = AppTextTone.SECONDARY
                                    )
                                } else {
                                    AppText(
                                        text = "Ready to backup alarms & sleep logs",
                                        variant = AppTextVariant.CAPTION,
                                        tone = AppTextTone.SECONDARY
                                    )
                                }
                            }
                        }

                        if (state.userEmail != null) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSignOut() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = "Sign Out",
                                    tint = LuneColors.textSoft,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Sign Out",
                                    fontSize = 12.sp,
                                    color = LuneColors.textSoft
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onOpenAuthDialog() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = "Sign In",
                                    tint = Color(0xFF3ECF8E),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Sign In",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E7E52)
                                )
                            }
                        }
                    }
                }
            } else {
                // Setup Callout
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFFF9E6))
                        .border(1.dp, Color(0xFFFFD56B), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF9A6E00),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Connect Your Supabase Project",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF594000)
                            )
                        }
                        Text(
                            text = "Add SUPABASE_URL and SUPABASE_ANON_KEY to your Secrets panel in AI Studio to enable cloud sync.",
                            fontSize = 12.sp,
                            color = Color(0xFF594000)
                        )
                    }
                }
            }

            // Sync message banner
            if (state.lastSyncMessage != null || state.errorMessage != null) {
                val isErr = state.errorMessage != null
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isErr) Color(0xFFFFECEF) else Color(0xFFE8F9F1))
                        .border(1.dp, if (isErr) Color(0xFFFFB2C5) else Color(0xFF3ECF8E), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.errorMessage ?: state.lastSyncMessage ?: "",
                            fontSize = 12.sp,
                            color = if (isErr) Color(0xFFB01D38) else Color(0xFF13643B),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Dismiss",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuneColors.textSoft,
                            modifier = Modifier
                                .clickable { onClearMessage() }
                                .padding(start = 8.dp)
                        )
                    }
                }
            }

            // Action Buttons: Backup & Restore
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Cloud Backup button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (state.isConfigured) Color(0xFF3ECF8E) else Color(0xFFE5DFEC))
                        .clickable(enabled = state.isConfigured && !state.isSyncing) { onBackup() }
                        .padding(vertical = 11.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (state.isSyncing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Backup",
                                tint = if (state.isConfigured) Color.White else LuneColors.textSoft,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Backup Now",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (state.isConfigured) Color.White else LuneColors.textSoft
                        )
                    }
                }

                // Cloud Restore button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.5.dp, if (state.isConfigured) Color(0xFF3ECF8E) else LuneColors.border, RoundedCornerShape(14.dp))
                        .clickable(enabled = state.isConfigured && !state.isSyncing) { onRestore() }
                        .padding(vertical = 11.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Restore",
                            tint = if (state.isConfigured) Color(0xFF1E7E52) else LuneColors.textSoft,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Restore Data",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (state.isConfigured) Color(0xFF1E7E52) else LuneColors.textSoft
                        )
                    }
                }
            }

            // Quick Setup Collapsible Guide
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showSetupGuide = !showSetupGuide }
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showSetupGuide) "Hide Supabase Table Schema Guide" else "View Supabase SQL & Secrets Guide",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LuneColors.primaryDeep
                )
                Icon(
                    imageVector = if (showSetupGuide) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = LuneColors.primaryDeep,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = showSetupGuide) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF6F3FA))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "1. AI Studio Secrets Panel:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = LuneColors.text
                    )
                    Text(
                        text = "Add SUPABASE_URL (e.g. https://xyz.supabase.co) and SUPABASE_ANON_KEY (your public anon key).",
                        fontSize = 11.sp,
                        color = LuneColors.textSoft
                    )
                    Text(
                        text = "2. Supabase SQL Schema (Run in SQL Editor):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = LuneColors.text
                    )
                    Text(
                        text = """create table if not exists alarms (
  id bigint primary key,
  user_id text,
  title text,
  hour int,
  minute int,
  repeat_days text,
  is_enabled boolean,
  wake_mode text,
  dismiss_type text,
  sound text,
  wallpaper text,
  strict_no_snooze boolean,
  created_at bigint
);

create table if not exists sleep_sessions (
  id bigint primary key,
  user_id text,
  started_at bigint,
  ended_at bigint,
  duration_minutes int,
  sleep_goal_met boolean,
  source text,
  notes text,
  movement_count int,
  deep_sleep_minutes int,
  light_sleep_minutes int,
  restless_minutes int,
  sleep_score int,
  movement_rating text
);""".trimIndent(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color(0xFF2C243B)
                    )
                }
            }
        }
    }
}
