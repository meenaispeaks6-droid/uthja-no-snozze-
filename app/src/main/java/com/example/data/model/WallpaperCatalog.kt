package com.example.data.model

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.R

data class WallpaperItem(
    val id: String,
    val title: String,
    val soundLabel: String,
    val category: String,
    val quote: String? = null,
    val quoteSub: String? = null,
    val drawableRes: Int? = null,
    val customUri: String? = null,
    val gradientColors: List<Color>,
    val accentColor: Color = Color(0xFF38BDF8),
    val iconEmoji: String = "✨"
)

object WallpaperCatalog {

    const val DEFAULT_WALLPAPER_ID = "wp_meow_alarm"

    val TRENDING_ITEMS = listOf(
        WallpaperItem(
            id = "wp_meow_alarm",
            title = "Meow Alarm",
            soundLabel = "Meow Alarm",
            category = "Trending",
            drawableRes = R.drawable.wp_trending_1,
            gradientColors = listOf(Color(0xFF3D271D), Color(0xFF78350F), Color(0xFFB45309), Color(0xFFD97706)),
            accentColor = Color(0xFFF59E0B),
            iconEmoji = "🐱"
        ),
        WallpaperItem(
            id = "wp_screaming_cat",
            title = "Screaming Cat",
            soundLabel = "Screaming Cat",
            category = "Trending",
            drawableRes = R.drawable.wp_trending_2,
            gradientColors = listOf(Color(0xFF1E112A), Color(0xFF3B1358), Color(0xFF5B21B6), Color(0xFF8B5CF6)),
            accentColor = Color(0xFFA855F7),
            iconEmoji = "🙀"
        ),
        WallpaperItem(
            id = "wp_dancing_cat",
            title = "Dancing cat",
            soundLabel = "Dancing cat",
            category = "Trending",
            drawableRes = R.drawable.wp_anime_1,
            gradientColors = listOf(Color(0xFF422006), Color(0xFF713F12), Color(0xFFA16207), Color(0xFFEAB308)),
            accentColor = Color(0xFFFBBF24),
            iconEmoji = "💃"
        ),
        WallpaperItem(
            id = "wp_study_alarm",
            title = "Daily Schedule",
            soundLabel = "Alarm...",
            category = "Trending",
            drawableRes = R.drawable.wp_minimal_1,
            gradientColors = listOf(Color(0xFF1E293B), Color(0xFF334155), Color(0xFF475569), Color(0xFF94A3B8)),
            accentColor = Color(0xFF38BDF8),
            iconEmoji = "📝"
        )
    )

    val MOTIVATION_ITEMS = listOf(
        WallpaperItem(
            id = "wp_wakeup_glow",
            title = "Wake up you...",
            soundLabel = "Wake up you...",
            category = "Motivation",
            quote = "Power within you",
            quoteSub = "Conquer today",
            drawableRes = R.drawable.wp_aesthetic_1,
            gradientColors = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF831843), Color(0xFFBE123C)),
            accentColor = Color(0xFFF43F5E),
            iconEmoji = "✊"
        ),
        WallpaperItem(
            id = "wp_life_better",
            title = "Life gets better",
            soundLabel = "Life gets be...",
            category = "Motivation",
            quote = "Keep going,\nlife gets better",
            quoteSub = "Peace & Sunshine",
            drawableRes = R.drawable.wp_aesthetic_2,
            gradientColors = listOf(Color(0xFF1E1B4B), Color(0xFF6B21A8), Color(0xFFC2410C), Color(0xFFFB923C)),
            accentColor = Color(0xFFFB923C),
            iconEmoji = "🌅"
        ),
        WallpaperItem(
            id = "wp_smile_twin",
            title = "Wake up & smile",
            soundLabel = "Wake up & s...",
            category = "Motivation",
            quote = "Smile twin,\nyou woke up",
            quoteSub = "A new opportunity begins",
            drawableRes = R.drawable.wp_nature_1,
            gradientColors = listOf(Color(0xFF0369A1), Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFFBAE6FD)),
            accentColor = Color(0xFF0284C7),
            iconEmoji = "🐶"
        ),
        WallpaperItem(
            id = "wp_study_ambition",
            title = "Focus & Ambition",
            soundLabel = "... to you",
            category = "Motivation",
            quote = "The future depends\non what you do today",
            quoteSub = "Deep Work",
            drawableRes = R.drawable.wp_minimal_2,
            gradientColors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155), Color(0xFF64748B)),
            accentColor = Color(0xFF94A3B8),
            iconEmoji = "📚"
        )
    )

    val SPACE_ITEMS = listOf(
        WallpaperItem(
            id = "wp_cosmic_nebula",
            title = "Cosmic Nebula",
            soundLabel = "Deep Space",
            category = "Space",
            drawableRes = R.drawable.wp_nature_2,
            gradientColors = listOf(Color(0xFF0B0F19), Color(0xFF1E1B4B), Color(0xFF4C1D95), Color(0xFF065F46)),
            accentColor = Color(0xFF34D399),
            iconEmoji = "🪐"
        ),
        WallpaperItem(
            id = "wp_aurora_dream",
            title = "Aurora Dream",
            soundLabel = "Aurora Glow",
            category = "Space",
            drawableRes = R.drawable.wp_anime_2,
            gradientColors = listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF0D9488), Color(0xFF2DD4BF)),
            accentColor = Color(0xFF2DD4BF),
            iconEmoji = "🌌"
        )
    )

    val ALL_WALLPAPERS: List<WallpaperItem> by lazy {
        TRENDING_ITEMS + MOTIVATION_ITEMS + SPACE_ITEMS
    }

    fun findById(id: String?): WallpaperItem {
        if (id == null) return TRENDING_ITEMS.first()
        // If it starts with "custom_uri:" then it's a user photo picked from device album
        if (id.startsWith("content://") || id.startsWith("file://") || id.startsWith("custom_uri:")) {
            val uri = id.removePrefix("custom_uri:")
            return WallpaperItem(
                id = id,
                title = "My Album Photo",
                soundLabel = "Custom Photo",
                category = "My Photos",
                customUri = uri,
                gradientColors = listOf(Color(0xFF0F172A), Color(0xFF1E293B)),
                accentColor = Color(0xFF38BDF8),
                iconEmoji = "📸"
            )
        }
        return ALL_WALLPAPERS.find { it.id.equals(id, ignoreCase = true) } ?: TRENDING_ITEMS.first()
    }
}
