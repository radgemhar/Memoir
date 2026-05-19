package com.example.memoir.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.sp

private val BaseTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    )
)

fun memoirTypography(fontScale: Float): Typography = BaseTypography.copy(
    displayLarge = BaseTypography.displayLarge.scaled(fontScale),
    displayMedium = BaseTypography.displayMedium.scaled(fontScale),
    displaySmall = BaseTypography.displaySmall.scaled(fontScale),
    headlineLarge = BaseTypography.headlineLarge.scaled(fontScale),
    headlineMedium = BaseTypography.headlineMedium.scaled(fontScale),
    headlineSmall = BaseTypography.headlineSmall.scaled(fontScale),
    titleLarge = BaseTypography.titleLarge.scaled(fontScale),
    titleMedium = BaseTypography.titleMedium.scaled(fontScale),
    titleSmall = BaseTypography.titleSmall.scaled(fontScale),
    bodyLarge = BaseTypography.bodyLarge.scaled(fontScale),
    bodyMedium = BaseTypography.bodyMedium.scaled(fontScale),
    bodySmall = BaseTypography.bodySmall.scaled(fontScale),
    labelLarge = BaseTypography.labelLarge.scaled(fontScale),
    labelMedium = BaseTypography.labelMedium.scaled(fontScale),
    labelSmall = BaseTypography.labelSmall.scaled(fontScale)
)

private fun TextStyle.scaled(scale: Float): TextStyle = copy(
    fontSize = fontSize.scaled(scale),
    lineHeight = lineHeight.scaled(scale)
)

private fun TextUnit.scaled(scale: Float): TextUnit {
    return if (type == TextUnitType.Sp) (value * scale).sp else this
}
