package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.alarm.rememberAppleHaptics

/**
 * Streak card using the exact uploaded UI artwork from the user:
 * - 12-day streak header with "You're glowing! 💖"
 * - Triple-bar menu icon in lilac
 * - Watercolor blossoming cherry tree with 3 purple birds
 * - Interactive dialog on tap celebrating streak consistency
 */
@Composable
fun StreakBlossomCard(
    modifier: Modifier = Modifier,
    streakDays: Int = 12,
    onClick: (() -> Unit)? = null
) {
    val haptics = rememberAppleHaptics()
    var showStreakDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(30.dp),
                spotColor = Color(0x188E69AA),
                ambientColor = Color(0x0A8E69AA)
            )
            .clip(RoundedCornerShape(30.dp))
            .background(Color(0xFFFFF9F8))
            .border(1.dp, Color(0xFFF7ECEB), RoundedCornerShape(30.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    haptics.pulseAppleButtonClick()
                    if (onClick != null) {
                        onClick()
                    } else {
                        showStreakDialog = true
                    }
                }
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.streak_tree_card),
            contentDescription = "12-day streak: You're glowing!",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1307f / 1536f)
                .clip(RoundedCornerShape(30.dp))
        )
    }

    // Streak Celebration Dialog
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
                color = Color(0xFFFFFDFC),
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
                                text = "Streak Blossom",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E232A)
                            )
                        }

                        IconButton(
                            onClick = {
                                haptics.pulseAppleButtonClick()
                                showStreakDialog = false
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFFFF1F2))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.streak_tree_card),
                            contentDescription = "Blossom Tree",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(20.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "$streakDays-Day Streak",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )

                    Text(
                        text = "You're glowing! 💖",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF9333EA),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Every consistent wake-up routine helps your habits blossom into full bloom. Keep it up tomorrow morning!",
                        fontSize = 14.5.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            haptics.pulseAppleButtonClick()
                            showStreakDialog = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE11D48),
                            contentColor = Color.White
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Keep Glowing",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
