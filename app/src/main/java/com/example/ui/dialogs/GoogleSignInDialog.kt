package com.example.ui.dialogs

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.GoogleIcon
import com.example.ui.components.LuneButton
import com.example.ui.components.LuneButtonVariant
import com.example.ui.theme.LuneColors

@Composable
fun GoogleSignInDialog(
    isLoading: Boolean,
    effectiveClientId: String?,
    onDismiss: () -> Unit,
    onSignInWithGoogle: (customClientId: String?) -> Unit,
    onSignInDemo: (email: String, name: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var customClientId by remember { mutableStateOf(effectiveClientId ?: "") }
    var isAdvancedExpanded by remember { mutableStateOf(effectiveClientId.isNullOrBlank()) }
    var demoEmail by remember { mutableStateOf("meenaispeaks6@gmail.com") }
    var demoName by remember { mutableStateOf("Meenai Speaks") }
    val clipboardManager = LocalClipboardManager.current

    val sha1Fingerprint = "8F:5E:02:27:6D:EA:7C:DD:42:B0:1C:6C:E9:7A:D6:94:E9:43:2F:4C"
    val packageName = "com.aistudio.lune.wake"

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .border(2.dp, LuneColors.border, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GoogleIcon(size = 26.dp)
                        Text(
                            text = "Sign In with Google",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = LuneColors.text
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = LuneColors.text
                        )
                    }
                }

                Text(
                    text = "Sign in to keep your alarms, wake-up streaks, and sleep analysis synchronized with your Google account.",
                    fontSize = 13.sp,
                    color = LuneColors.textSoft,
                    lineHeight = 18.sp
                )

                // Primary Google Sign-In button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.5.dp, Color(0xFF4285F4), RoundedCornerShape(14.dp))
                        .clickable(enabled = !isLoading) {
                            onSignInWithGoogle(customClientId.ifBlank { null })
                        }
                        .padding(vertical = 14.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF4285F4),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GoogleIcon(size = 22.dp)
                            Text(
                                text = "Continue with Google",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF1F1F1F)
                            )
                        }
                    }
                }

                // Expandable Setup & Quick-Test Panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF7F6FB))
                        .border(1.dp, LuneColors.border, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isAdvancedExpanded = !isAdvancedExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF6B4EFF),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Firebase & Client ID Setup",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B4EFF)
                            )
                        }
                        Icon(
                            imageVector = if (isAdvancedExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = LuneColors.textSoft,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AnimatedVisibility(visible = isAdvancedExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "If you don't have google-services.json in /app yet, paste your Web Client ID below, or use the 1-Tap Google Login for instant testing.",
                                fontSize = 11.sp,
                                color = LuneColors.textSoft,
                                lineHeight = 15.sp
                            )

                            OutlinedTextField(
                                value = customClientId,
                                onValueChange = { customClientId = it },
                                label = { Text("Web Client ID (from Firebase Console)") },
                                placeholder = { Text("e.g. 12345-abc.apps.googleusercontent.com") },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF4285F4),
                                    unfocusedBorderColor = LuneColors.border
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Quick Fingerprint reminder
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.dp, LuneColors.border, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Your App Package: $packageName",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = LuneColors.text
                                    )
                                    Text(
                                        text = "SHA-1: $sha1Fingerprint",
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = LuneColors.textSoft
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(sha1Fingerprint))
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy SHA-1",
                                        tint = Color(0xFF6B4EFF),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            // 1-Tap Login for Emulator
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFEDE7F6))
                                    .clickable {
                                        onSignInDemo(demoEmail, demoName)
                                    }
                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "⚡ Instant Sign In as $demoEmail",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF512DA8)
                                )
                            }
                        }
                    }
                }

                // Cancel button
                LuneButton(
                    onClick = onDismiss,
                    variant = LuneButtonVariant.SECONDARY,
                    fullWidth = true
                ) {
                    Text(
                        text = "Cancel",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = LuneColors.textSoft
                    )
                }
            }
        }
    }
}
