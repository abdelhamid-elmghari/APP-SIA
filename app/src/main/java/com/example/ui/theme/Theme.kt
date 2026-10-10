package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val StitchLightColorScheme = lightColorScheme(
    primary = Terracotta,
    onPrimary = Color.White,
    primaryContainer = TerracottaContainer,
    onPrimaryContainer = OnTerracottaContainer,
    secondary = OasisGreen,
    onSecondary = Color.White,
    secondaryContainer = OasisGreenContainer,
    onSecondaryContainer = OnOasisGreenContainer,
    tertiary = TwilightIndigo,
    onTertiary = Color.White,
    tertiaryContainer = TwilightIndigoContainer,
    onTertiaryContainer = OnTwilightIndigoContainer,
    background = WarmCanvas,
    onBackground = TextDark,
    surface = WarmCanvas,
    onSurface = TextDark,
    surfaceVariant = CardWhite,
    onSurfaceVariant = MutedText,
    outline = BorderColor,
    outlineVariant = BorderSoft,
    surfaceContainer = CardWhite,
    surfaceContainerHigh = CardWhite,
    surfaceContainerLow = WarmCanvas
)

private val StitchDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB59B),
    onPrimary = Color(0xFF581E00),
    primaryContainer = Terracotta,
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = Color(0xFF96D4B2),
    onSecondary = Color(0xFF003822),
    secondaryContainer = OasisGreen,
    onSecondaryContainer = Color(0xFFB1F0CD),
    tertiary = Color(0xFFB7C7EA),
    onTertiary = Color(0xFF1E2E4A),
    background = Color(0xFF181A1B),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF1F2223),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF282B2D),
    onSurfaceVariant = Color(0xFFC4C7C5),
    outline = Color(0xFF3E4347)
)

@Composable
fun TimizarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep authentic Warm Kasbah Stitch brand colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> StitchDarkColorScheme
        else -> StitchLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
