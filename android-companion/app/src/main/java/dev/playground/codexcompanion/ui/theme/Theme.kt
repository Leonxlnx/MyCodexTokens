package dev.playground.codexcompanion.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.playground.codexcompanion.data.ThemeMode

private val DarkColors =
    darkColorScheme(
        primary = MintAccent,
        onPrimary = InkText,
        secondary = SignalAmber,
        onSecondary = InkText,
        tertiary = Color(0xFF95B8FF),
        background = InkBlack,
        onBackground = Snow,
        surface = InkPanel,
        onSurface = Snow,
        surfaceVariant = InkPanelRaised,
        onSurfaceVariant = Color(0xFFCAD6DF),
        outline = SlateOutline,
    )

private val LightColors =
    lightColorScheme(
        primary = MintDeep,
        onPrimary = Paper,
        secondary = Color(0xFF7A5200),
        onSecondary = Paper,
        tertiary = Color(0xFF26457A),
        background = Snow,
        onBackground = InkText,
        surface = Paper,
        onSurface = InkText,
        surfaceVariant = Mist,
        onSurfaceVariant = Color(0xFF30424F),
        outline = Color(0xFF667D8D),
    )

private val CompanionTypography =
    Typography(
        displaySmall =
            TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                lineHeight = 38.sp,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                lineHeight = 28.sp,
            ),
        titleLarge =
            TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 24.sp,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
        labelLarge =
            TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            ),
    )

@Composable
fun CodexCompanionTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val useDarkTheme =
        when (themeMode) {
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
        }

    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkColors else LightColors,
        typography = CompanionTypography,
        shapes = Shapes(),
        content = content,
    )
}
