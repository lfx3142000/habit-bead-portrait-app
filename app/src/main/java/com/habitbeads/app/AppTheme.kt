package com.habitbeads.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class AppThemeChoice(val label: String) {
    Warm("Warm"),
    Ocean("Ocean"),
    Forest("Forest"),
    Grape("Grape")
}

private val WarmColorScheme = lightColorScheme(
    primary = Color(0xFF277DA1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9EEF6),
    onPrimaryContainer = Color(0xFF12343F),
    secondary = Color(0xFF2A9D8F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDF4EE),
    onSecondaryContainer = Color(0xFF123B35),
    tertiary = Color(0xFFB56576),
    onTertiary = Color.White,
    background = Color(0xFFFFFBF6),
    onBackground = Color(0xFF241F1B),
    surface = Color(0xFFFFFBF6),
    onSurface = Color(0xFF241F1B),
    surfaceVariant = Color(0xFFF1E6DA),
    onSurfaceVariant = Color(0xFF5A5148),
    outline = Color(0xFF8C7D70)
)

private val OceanColorScheme = lightColorScheme(
    primary = Color(0xFF176B87),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4F1F4),
    onPrimaryContainer = Color(0xFF082F3A),
    secondary = Color(0xFF2C7DA0),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F4FF),
    onSecondaryContainer = Color(0xFF16384A),
    tertiary = Color(0xFF61A5C2),
    background = Color(0xFFF7FCFF),
    onBackground = Color(0xFF172126),
    surface = Color(0xFFF7FCFF),
    onSurface = Color(0xFF172126),
    surfaceVariant = Color(0xFFE7F0F4),
    onSurfaceVariant = Color(0xFF45545B),
    outline = Color(0xFF71828A)
)

private val ForestColorScheme = lightColorScheme(
    primary = Color(0xFF386641),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDEAD7),
    onPrimaryContainer = Color(0xFF1B331F),
    secondary = Color(0xFF6A994E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEAF4E4),
    onSecondaryContainer = Color(0xFF273B1C),
    tertiary = Color(0xFFA7C957),
    background = Color(0xFFFFFDF6),
    onBackground = Color(0xFF202217),
    surface = Color(0xFFFFFDF6),
    onSurface = Color(0xFF202217),
    surfaceVariant = Color(0xFFEEEBDD),
    onSurfaceVariant = Color(0xFF555244),
    outline = Color(0xFF817C6B)
)

private val GrapeColorScheme = lightColorScheme(
    primary = Color(0xFF6D597A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE3F2),
    onPrimaryContainer = Color(0xFF33283A),
    secondary = Color(0xFFB56576),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF8E2E7),
    onSecondaryContainer = Color(0xFF4A2830),
    tertiary = Color(0xFFE56B6F),
    background = Color(0xFFFFFAFC),
    onBackground = Color(0xFF251F24),
    surface = Color(0xFFFFFAFC),
    onSurface = Color(0xFF251F24),
    surfaceVariant = Color(0xFFF2E7EC),
    onSurfaceVariant = Color(0xFF594D54),
    outline = Color(0xFF897A83)
)

@Composable
fun HabitBeadsApp() {
    val context = LocalContext.current
    var themeChoice by remember {
        mutableStateOf(
            AppThemeChoice.values().firstOrNull { it.name == loadSavedThemeName(context) } ?: AppThemeChoice.Warm
        )
    }

    val scheme = when (themeChoice) {
        AppThemeChoice.Warm -> WarmColorScheme
        AppThemeChoice.Ocean -> OceanColorScheme
        AppThemeChoice.Forest -> ForestColorScheme
        AppThemeChoice.Grape -> GrapeColorScheme
    }

    MaterialTheme(colorScheme = scheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            HabitTrackerScreen(
                themeChoice = themeChoice,
                onThemeChoiceChange = { choice ->
                    themeChoice = choice
                    saveThemeName(context, choice.name)
                }
            )
        }
    }
}
