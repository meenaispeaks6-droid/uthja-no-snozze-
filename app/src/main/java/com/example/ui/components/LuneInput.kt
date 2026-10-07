package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LuneColors

@Composable
fun LuneInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    helper: String? = null,
    error: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val borderColor = if (!error.isNullOrBlank()) LuneColors.coral else LuneColors.border

    Column(modifier = modifier) {
        if (!label.isNullOrBlank()) {
            AppText(
                text = label,
                variant = AppTextVariant.LABEL,
                tone = AppTextTone.DEFAULT,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 52.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(LuneColors.surface)
                .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty() && placeholder.isNotEmpty()) {
                AppText(
                    text = placeholder,
                    variant = AppTextVariant.BODY,
                    tone = AppTextTone.MUTED
                )
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                singleLine = singleLine,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = LuneColors.text
                ),
                cursorBrush = SolidColor(LuneColors.primaryDeep),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (!helper.isNullOrBlank() && error.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            AppText(
                text = helper,
                variant = AppTextVariant.CAPTION,
                tone = AppTextTone.MUTED
            )
        }

        if (!error.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            AppText(
                text = error,
                variant = AppTextVariant.CAPTION,
                tone = AppTextTone.DANGER
            )
        }
    }
}
