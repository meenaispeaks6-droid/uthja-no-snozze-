package com.example.sleep

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundPickerBottomSheet(
    selectedSound: SleepSoundItem,
    isPlaying: Boolean,
    timerMinutes: Int?,
    onSoundSelected: (SleepSoundItem) -> Unit,
    onSetTimerMinutes: (Int?) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredSounds = remember(selectedCategory) {
        when (selectedCategory) {
            "All" -> SoundCatalog.ALL_SOUNDS
            "Nature" -> SoundCatalog.ALL_SOUNDS.filter { it.category == "Nature" || it.id == "no_sound" }
            "Meditation" -> SoundCatalog.ALL_SOUNDS.filter { it.category == "Meditation" || it.id == "no_sound" }
            "White noise" -> SoundCatalog.ALL_SOUNDS.filter { it.category == "White noise" || it.id == "no_sound" }
            "Recent" -> listOf(SoundCatalog.LIGHTHOUSE_KEEPER, SoundCatalog.LIGHT_RAIN, SoundCatalog.NO_SOUND)
            else -> SoundCatalog.ALL_SOUNDS
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF18191C),
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("sound_picker_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            // Top Bar with Close 'X' Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("sound_picker_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFFA3A3A3),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Now Playing Mini Player
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF212226))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .testTag("now_playing_mini_player")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Thumbnail
                        Box(
                            modifier = Modifier
                                .size(width = 60.dp, height = 46.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF262626))
                        ) {
                            if (selectedSound.drawableResId != null) {
                                Image(
                                    painter = painterResource(id = selectedSound.drawableResId),
                                    contentDescription = selectedSound.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFF1B223D)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_music_off),
                                        contentDescription = null,
                                        tint = Color(0xFF4C6EF5),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Title & Duration / Status
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedSound.title,
                                color = Color(0xFFF5F5F5),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val timerText = if (timerMinutes != null) "${timerMinutes}m remaining" else selectedSound.durationText
                            Text(
                                text = if (isPlaying) "Playing • $timerText" else timerText,
                                color = Color(0xFFA3A3A3),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Timer / Stopwatch Action Button
                    val nextTimer = when (timerMinutes) {
                        null -> 15
                        15 -> 30
                        30 -> 60
                        else -> null
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (timerMinutes != null) Color(0xFF4B75FF) else Color(0xFF2E3036)
                            )
                            .clickable { onSetTimerMinutes(nextTimer) }
                            .testTag("sound_timer_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Timer settings",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Filter Tabs (Horizontal Scroll)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SoundCatalog.CATEGORIES.forEach { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color.White else Color(0xFF212226))
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 16.dp, vertical = 9.dp)
                            .testTag("filter_tab_$category"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) Color.Black else Color(0xFFD4D4D4),
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sound Cards Grid (2 Columns)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredSounds, key = { it.id }) { sound ->
                    val isCurrent = selectedSound.id == sound.id
                    SoundGridCard(
                        sound = sound,
                        isSelected = isCurrent,
                        isPlaying = isCurrent && isPlaying,
                        onClick = { onSoundSelected(sound) }
                    )
                }
            }
        }
    }
}

@Composable
fun SoundGridCard(
    sound: SleepSoundItem,
    isSelected: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_anim")
    val barScale1 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(450), RepeatMode.Reverse),
        label = "b1"
    )
    val barScale2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(550), RepeatMode.Reverse),
        label = "b2"
    )
    val barScale3 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(400), RepeatMode.Reverse),
        label = "b3"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1C1D21))
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) Color(0xFF46C3DD) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .testTag("sound_card_${sound.id}")
    ) {
        // Visual Artwork Container (Height 120dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(116.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(if (sound.id == "no_sound") Color(0xFF1B223D) else Color(0xFF262626))
        ) {
            if (sound.drawableResId != null) {
                Image(
                    painter = painterResource(id = sound.drawableResId),
                    contentDescription = sound.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // "No sound" icon
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_music_off),
                        contentDescription = "No sound icon",
                        tint = Color(0xFF4C6EF5),
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            // Selected Checkmark Badge (Top Right)
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF46C3DD)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Animated waveform indicator when active / playing
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height((12 * barScale1).dp.coerceAtLeast(3.dp))
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height((18 * barScale2).dp.coerceAtLeast(4.dp))
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height((10 * barScale3).dp.coerceAtLeast(3.dp))
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height((20 * barScale1).dp.coerceAtLeast(5.dp))
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height((12 * barScale2).dp.coerceAtLeast(3.dp))
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }
            }
        }

        // Card Details Footer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF16171A))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = sound.title,
                color = Color(0xFFF5F5F5),
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (sound.tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = sound.tags,
                    color = Color(0xFF8E9096),
                    fontSize = 10.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (sound.creator.isNotBlank()) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = sound.creator,
                    color = Color(0xFF6C6E75),
                    fontSize = 10.5.sp,
                    maxLines = 1
                )
            }
        }
    }
}
