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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.auth.GoogleAuthState
import com.example.ui.theme.LuneColors

@Composable
fun GoogleSignInCard(
    authState: GoogleAuthState,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onOpenConfigDialog: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    LuneCard(
        modifier = modifier.fillMaxWidth(),
        elevated = true
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GoogleIcon(size = 22.dp)
                    Text(
                        text = "Google Account",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = LuneColors.text
                    )
                }

                if (authState.isSignedIn) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFE6F8EF))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Active",
                                tint = Color(0xFF1B8755),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Connected",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1B8755)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFF1EEFA))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Not Connected",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = LuneColors.textSoft
                        )
                    }
                }
            }

            // Error Banner
            AnimatedVisibility(visible = authState.errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF0F0))
                        .border(1.dp, Color(0xFFFFD1D1), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = authState.errorMessage ?: "",
                            fontSize = 12.sp,
                            color = Color(0xFFD93025),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onClearError,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = Color(0xFFD93025),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Body Content
            if (authState.isSignedIn && authState.user != null) {
                val user = authState.user
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFAF9FD))
                        .border(1.dp, LuneColors.border, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!user.photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = user.photoUrl,
                            contentDescription = "User Avatar",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Color(0xFF4285F4), CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4285F4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (user.displayName?.take(1) ?: user.email.take(1)).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.displayName ?: "Google User",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = LuneColors.text
                        )
                        Text(
                            text = user.email,
                            fontSize = 13.sp,
                            color = LuneColors.textSoft
                        )
                    }

                    IconButton(
                        onClick = onSignOutClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = Color(0xFFD93025)
                        )
                    }
                }
            } else {
                Text(
                    text = "Sign in to your account with Google to sync your alarms, bedtime habits, and morning sleep reports across your devices.",
                    fontSize = 13.sp,
                    color = LuneColors.textSoft,
                    lineHeight = 18.sp
                )

                // Sign in with Google Primary Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.5.dp, Color(0xFFDADCE0), RoundedCornerShape(14.dp))
                        .clickable(enabled = !authState.isLoading) { onSignInClick() }
                        .padding(vertical = 13.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (authState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color(0xFF4285F4),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GoogleIcon(size = 20.dp)
                            Text(
                                text = "Sign in with Google",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF3C4043)
                            )
                        }
                    }
                }

                // Setup / Help Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenConfigDialog() }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Options",
                        tint = LuneColors.textSoft,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = " Google Sign-In options & setup helper",
                        fontSize = 12.sp,
                        color = LuneColors.textSoft,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
