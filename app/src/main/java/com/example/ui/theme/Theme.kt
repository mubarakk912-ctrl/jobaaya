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

private val DarkColorScheme =
  darkColorScheme(
    primary = PineGreen,
    onPrimary = Color.White,
    primaryContainer = HunterGreenDark,
    onPrimaryContainer = Color.White,
    secondary = FoxtonsDarkGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF00281F),
    onSecondaryContainer = Color.White,
    tertiary = HunterGreenLight,
    onTertiary = Color.White,
    background = Color(0xFF001A05),
    surface = Color(0xFF001A05),
    onBackground = Color.White,
    onSurface = OffWhite
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PineGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8F5E9),
    onPrimaryContainer = PineGreen,
    secondary = FoxtonsDarkGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCEDC8),
    onSecondaryContainer = Color(0xFF33691E),
    tertiary = HunterGreen,
    onTertiary = Color.White,
    background = Color(0xFF001A05), // Keeping it dark as per user preference for white headings
    surface = Color(0xFF001A05),
    onBackground = Color.White,
    onSurface = OffWhite
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color disabled to ensure Pine Green is always used
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = com.example.ui.theme.Typography,
    content = content
  )
}
