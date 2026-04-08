package com.azkari.wasalati

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val TajawalFamily = FontFamily(
    Font(R.font.tajawal_regular, FontWeight.Normal),
    Font(R.font.tajawal_bold, FontWeight.Bold),
)

val AmiriFamily = FontFamily(
    Font(R.font.amiri_regular, FontWeight.Normal),
    Font(R.font.amiri_bold, FontWeight.Bold),
)

val Forest = Color(0xFF065F46)
val ForestDark = Color(0xFF052E16)
val ForestMid = Color(0xFF059669)
val ForestLight = Color(0xFF10B981)
val Sage = Color(0xFFD1FAE5)
val Cream = Color(0xFFFAFAF8)
val SurfaceWarm = Color(0xFFFFFCF6)
val SurfaceSoft = Color(0xFFF0FDF4)
val Gold = Color(0xFFB45309)
val GoldBright = Color(0xFFD4A853)
val GoldLight = Color(0xFFFEF3C7)
val AmberText = Color(0xFF9A7B4F)
val Warning = Color(0xFF9A3412)
val Ink = Color(0xFF1C1917)
val QuranInk = Color(0xFF0D1117)

private val AzkariColors = lightColorScheme(
    primary = Forest,
    onPrimary = Color.White,
    secondary = Gold,
    onSecondary = Color.White,
    background = Cream,
    onBackground = Ink,
    surface = SurfaceWarm,
    onSurface = Ink,
    surfaceVariant = SurfaceSoft,
    onSurfaceVariant = Color(0xFF5F6B64),
)

private val AzkariTypography = Typography(
    displayLarge = TextStyle(fontFamily = TajawalFamily, fontWeight = FontWeight.Bold, fontSize = 34.sp),
    displayMedium = TextStyle(fontFamily = TajawalFamily, fontWeight = FontWeight.Bold, fontSize = 30.sp),
    displaySmall = TextStyle(fontFamily = TajawalFamily, fontWeight = FontWeight.Bold, fontSize = 26.sp),
    headlineLarge = TextStyle(fontFamily = TajawalFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp),
    headlineMedium = TextStyle(fontFamily = TajawalFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    headlineSmall = TextStyle(fontFamily = TajawalFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp),
    titleLarge = TextStyle(fontFamily = TajawalFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp),
    titleMedium = TextStyle(fontFamily = TajawalFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp),
    titleSmall = TextStyle(fontFamily = TajawalFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp),
    bodyLarge = TextStyle(fontFamily = TajawalFamily, fontSize = 16.sp, lineHeight = 31.sp),
    bodyMedium = TextStyle(fontFamily = TajawalFamily, fontSize = 14.sp, lineHeight = 24.sp),
    bodySmall = TextStyle(fontFamily = TajawalFamily, fontSize = 12.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = TajawalFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp),
    labelMedium = TextStyle(fontFamily = TajawalFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp),
    labelSmall = TextStyle(fontFamily = TajawalFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp),
)

@Composable
fun AzkariFaithfulTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AzkariColors,
        typography = AzkariTypography,
        content = content,
    )
}
