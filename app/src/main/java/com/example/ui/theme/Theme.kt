package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val JarvisColorScheme = darkColorScheme(
  primary = JarvisCyan,
  onPrimary = JarvisBackground,
  primaryContainer = JarvisSurfaceElevated,
  onPrimaryContainer = JarvisCyanLight,
  secondary = JarvisCobalt,
  onSecondary = JarvisTextPrimary,
  secondaryContainer = JarvisSurfaceVariant,
  onSecondaryContainer = JarvisTextPrimary,
  tertiary = JarvisEmerald,
  onTertiary = JarvisBackground,
  background = JarvisBackground,
  onBackground = JarvisTextPrimary,
  surface = JarvisSurface,
  onSurface = JarvisTextPrimary,
  surfaceVariant = JarvisSurfaceVariant,
  onSurfaceVariant = JarvisTextSecondary,
  outline = JarvisBorder,
  outlineVariant = JarvisBorderBright,
  error = JarvisCrimson,
  onError = JarvisBackground
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = JarvisColorScheme,
    typography = Typography,
    content = content
  )
}

