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

private val DarkColorScheme =
    darkColorScheme(
        primary = NimbusSky500,
        onPrimary = androidx.compose.ui.graphics.Color.White,
        primaryContainer = NimbusSky700,
        onPrimaryContainer = NimbusSky100,
        secondary = Slate500,
        background = NimbusMidnight900,
        surface = NimbusMidnight800,
        onBackground = Slate50,
        onSurface = Slate50,
        surfaceVariant = NimbusMidnight700,
        onSurfaceVariant = Slate200,
        outline = Slate500
    )

private val LightColorScheme =
    lightColorScheme(
        primary = NimbusSky600,
        onPrimary = androidx.compose.ui.graphics.Color.White,
        primaryContainer = NimbusSky50,
        onPrimaryContainer = NimbusSky700,
        secondary = Slate500,
        background = androidx.compose.ui.graphics.Color(0xFFF0F7FF),
        surface = androidx.compose.ui.graphics.Color.White,
        onBackground = Slate900,
        onSurface = Slate900,
        surfaceVariant = Slate100,
        onSurfaceVariant = Slate700,
        outline = Slate200,
        outlineVariant = Slate100
    )

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
