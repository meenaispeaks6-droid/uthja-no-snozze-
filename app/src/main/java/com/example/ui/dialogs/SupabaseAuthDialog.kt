package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.AppText
import com.example.ui.components.AppTextTone
import com.example.ui.components.AppTextVariant
import com.example.ui.components.LuneButton
import com.example.ui.components.LuneButtonVariant
import com.example.ui.theme.LuneColors

@Composable
fun SupabaseAuthDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSignIn: (email: String, pass: String) -> Unit,
    onSignUp: (email: String, pass: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Sign In, 1 = Sign Up
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .border(2.dp, LuneColors.border, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header with Emerald Supabase Badge & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3ECF8E).copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = "⚡",
                                fontSize = 14.sp
                            )
                        }
                        AppText(
                            text = "Supabase Auth",
                            variant = AppTextVariant.TITLE_SMALL
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = LuneColors.text
                        )
                    }
                }

                // Tabs: Sign In vs Create Account
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFFF6F3FA),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color(0xFF3ECF8E),
                            height = 3.dp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            localError = null
                        },
                        text = {
                            Text(
                                text = "Sign In",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) LuneColors.text else LuneColors.textSoft
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            localError = null
                        },
                        text = {
                            Text(
                                text = "Create Account",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) LuneColors.text else LuneColors.textSoft
                            )
                        }
                    )
                }

                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        localError = null
                    },
                    label = { Text("Email address") },
                    leadingIcon = {
                        Icon(Icons.Default.Mail, contentDescription = "Email", tint = LuneColors.textSoft)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3ECF8E),
                        unfocusedBorderColor = LuneColors.border
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        localError = null
                    },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = "Password", tint = LuneColors.textSoft)
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                tint = LuneColors.textSoft
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3ECF8E),
                        unfocusedBorderColor = LuneColors.border
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Local validation error message
                if (localError != null) {
                    AppText(
                        text = localError!!,
                        variant = AppTextVariant.CAPTION,
                        tone = AppTextTone.DANGER
                    )
                }

                // Submit action button
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color(0xFF3ECF8E),
                        modifier = Modifier.size(36.dp)
                    )
                } else {
                    LuneButton(
                        onClick = {
                            if (email.isBlank() || !email.contains("@")) {
                                localError = "Please enter a valid email address."
                                return@LuneButton
                            }
                            if (password.length < 6) {
                                localError = "Password must be at least 6 characters."
                                return@LuneButton
                            }
                            if (selectedTab == 0) {
                                onSignIn(email.trim(), password)
                            } else {
                                onSignUp(email.trim(), password)
                            }
                        },
                        variant = LuneButtonVariant.PRIMARY,
                        fullWidth = true
                    ) {
                        Text(
                            text = if (selectedTab == 0) "Sign In to Supabase" else "Create Supabase Account",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
