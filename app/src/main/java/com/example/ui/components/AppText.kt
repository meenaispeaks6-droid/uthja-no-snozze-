package com.example.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LuneColors

enum class AppTextVariant {
    HERO,
    DISPLAY,
    TITLE,
    TITLE_SMALL,
    BODY_LARGE,
    BODY,
    BODY_SMALL,
    CAPTION,
    LABEL,
    EYEBROW
}

enum class AppTextTone {
    DEFAULT,
    SECONDARY,
    MUTED,
    INVERSE,
    ACCENT,
    DANGER,
    SUCCESS,
    WARNING
}

fun resolveToneColor(tone: AppTextTone): Color = when (tone) {
    AppTextTone.DEFAULT -> LuneColors.text
    AppTextTone.SECONDARY -> LuneColors.textSoft
    AppTextTone.MUTED -> LuneColors.textMuted
    AppTextTone.INVERSE -> LuneColors.white
    AppTextTone.ACCENT -> LuneColors.primaryDeep
    AppTextTone.DANGER -> LuneColors.coral
    AppTextTone.SUCCESS -> LuneColors.sageDark
    AppTextTone.WARNING -> LuneColors.goldDark
}

fun resolveVariantStyle(variant: AppTextVariant): TextStyle = when (variant) {
    AppTextVariant.HERO -> TextStyle(
        fontSize = 38.sp,
        lineHeight = 44.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp
    )
    AppTextVariant.DISPLAY -> TextStyle(
        fontSize = 42.sp,
        lineHeight = 48.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp
    )
    AppTextVariant.TITLE -> TextStyle(
        fontSize = 28.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp
    )
    AppTextVariant.TITLE_SMALL -> TextStyle(
        fontSize = 20.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.SemiBold
    )
    AppTextVariant.BODY_LARGE -> TextStyle(
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Medium
    )
    AppTextVariant.BODY -> TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Normal
    )
    AppTextVariant.BODY_SMALL -> TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal
    )
    AppTextVariant.CAPTION -> TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium
    )
    AppTextVariant.LABEL -> TextStyle(
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
    AppTextVariant.EYEBROW -> TextStyle(
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp
    )
}

@Composable
fun AppText(
    text: String,
    modifier: Modifier = Modifier,
    variant: AppTextVariant = AppTextVariant.BODY,
    tone: AppTextTone = AppTextTone.DEFAULT,
    color: Color? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    val baseStyle = resolveVariantStyle(variant)
    val resolvedColor = color ?: resolveToneColor(tone)
    val displayText = if (variant == AppTextVariant.LABEL || variant == AppTextVariant.EYEBROW) {
        text.uppercase()
    } else {
        text
    }

    Text(
        text = displayText,
        modifier = modifier,
        style = baseStyle.copy(
            color = resolvedColor,
            fontSize = if (fontSize != TextUnit.Unspecified) fontSize else baseStyle.fontSize,
            fontWeight = fontWeight ?: baseStyle.fontWeight,
            textAlign = textAlign ?: baseStyle.textAlign
        ),
        maxLines = maxLines,
        overflow = overflow
    )
}
