package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme =
  lightColorScheme(
    primary = PrimaryPurple,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerDark,
    background = BackgroundLight,
    onBackground = TextDark,
    surface = BackgroundLight,
    onSurface = TextDark,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = MutedText,
    outline = OutlineColor,
    outlineVariant = OutlineVariantColor,
    error = ErrorColor,
    onError = androidx.compose.ui.graphics.Color.White
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Force light theme for this specific design
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = false, // Disable dynamic color to enforce theme
  content: @Composable () -> Unit,
) {
  val colorScheme = LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
