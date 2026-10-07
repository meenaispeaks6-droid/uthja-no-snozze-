package com.example.sleep

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@Composable
fun SleepGuidanceScreen(
    onGotIt: () -> Unit,
    onNeverShowAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF18233C),
                        Color(0xFF0D1222),
                        Color(0xFF06080E)
                    ),
                    radius = 1200f
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Ambient background stars
        Box(
            modifier = Modifier
                .offset(x = 60.dp, y = 70.dp)
                .size(4.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.45f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-55).dp, y = 110.dp)
                .size(5.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.5f))
        )
        Box(
            modifier = Modifier
                .offset(x = 85.dp, y = 180.dp)
                .size(3.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.6f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-100).dp, y = 160.dp)
                .size(2.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.35f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-60).dp, y = (-80).dp)
                .size(4.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.4f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 40.dp, y = 120.dp)
                .size(3.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.35f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-70).dp, y = (-160).dp)
                .size(5.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.3f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp)
            ) {
                Text(
                    text = "Place your phone\nnext to your pillow",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 34.sp,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Keep it charging overnight",
                    color = Color(0xFFD1D5DB),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.2.sp
                )
            }

            // Center Sleep Illustration with Glow
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Subtle radial glow backdrop
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .blur(32.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF44567E).copy(alpha = 0.35f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // Bed Pillow & Phone image
                Image(
                    painter = painterResource(id = R.drawable.img_sleep_guidance_pillow),
                    contentDescription = "Phone charging on a soft pillow overnight",
                    modifier = Modifier
                        .widthIn(max = 320.dp)
                        .aspectRatio(1f)
                        .padding(16.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // Actions Footer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Button(
                    onClick = onGotIt,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("guidance_got_it_button"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF030712)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Text(
                        text = "I got it",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Never show again",
                    color = Color(0xFFD1D5DB),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.2.sp,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onNeverShowAgain
                        )
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                        .testTag("guidance_never_show_again_button")
                )
            }
        }
    }
}
