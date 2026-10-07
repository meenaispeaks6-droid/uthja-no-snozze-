package com.example.sleep

import com.example.R

object SoundCatalog {
    val NO_SOUND = SleepSoundItem(
        id = "no_sound",
        title = "No sound",
        tags = "",
        creator = "",
        durationText = "--:--",
        category = "All"
    )

    val LIGHT_RAIN = SleepSoundItem(
        id = "light_rain",
        title = "Light Rain",
        tags = "#Nature #Rain",
        creator = "Alarmy",
        durationText = "45:00",
        drawableResId = R.drawable.sound_light_rain,
        category = "Nature"
    )

    val WHITE_NOISE = SleepSoundItem(
        id = "white_noise",
        title = "Light white noise",
        tags = "#White noise #Relaxing",
        creator = "Alarmy",
        durationText = "60:00",
        drawableResId = R.drawable.sound_white_noise,
        category = "White noise"
    )

    val LIGHTHOUSE_KEEPER = SleepSoundItem(
        id = "lighthouse_keeper",
        title = "The Lighthouse Keeper",
        tags = "#Music #Relaxing",
        creator = "Healingbeats",
        durationText = "29:29",
        drawableResId = R.drawable.sound_lighthouse_keeper,
        category = "Meditation"
    )

    val BATH_SALT = SleepSoundItem(
        id = "bath_salt",
        title = "Bath salt dissolving",
        tags = "#Relaxing #Spa",
        creator = "Healingbeats",
        durationText = "30:00",
        drawableResId = R.drawable.sound_bath_salt,
        category = "Meditation"
    )

    val DAY1_GUIDE = SleepSoundItem(
        id = "day1_guide",
        title = "Day 1 Basic Guide",
        tags = "#Guide #Sleep",
        creator = "Alarmy",
        durationText = "15:00",
        drawableResId = R.drawable.sound_day1_guide,
        category = "Meditation"
    )

    val ALL_SOUNDS = listOf(
        NO_SOUND,
        LIGHT_RAIN,
        WHITE_NOISE,
        LIGHTHOUSE_KEEPER,
        BATH_SALT,
        DAY1_GUIDE
    )

    val CATEGORIES = listOf("Recent", "All", "Nature", "Meditation", "White noise")
}
