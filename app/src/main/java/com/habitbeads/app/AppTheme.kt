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
    Warm("Linen"),
    Ocean("Mist"),
    Forest("Sage"),
    Grape("Dusk")
}

fun habitColorsForTheme(choice: AppThemeChoice): List<Color> {
    return when (choice) {
        AppThemeChoice.Warm -> listOf(
            Color(0xFFB76E5F),
            Color(0xFFC2945F),
            Color(0xFF6F9A8D),
            Color(0xFF4F7E8A),
            Color(0xFF8C7A99),
            Color(0xFFAA7A88),
            Color(0xFF8D9568)
        )
        AppThemeChoice.Ocean -> listOf(
            Color(0xFF4E7F92),
            Color(0xFF5C91A6),
            Color(0xFF738AA0),
            Color(0xFF6D9CA0),
            Color(0xFF7B8DB8),
            Color(0xFF8BA6B8),
            Color(0xFF6C8E96)
        )
        AppThemeChoice.Forest -> listOf(
            Color(0xFF5E7D5B),
            Color(0xFF7F8F66),
            Color(0xFF8FA36A),
            Color(0xFF6D8F7E),
            Color(0xFFA3A77A),
            Color(0xFF9A8468),
            Color(0xFF78906E)
        )
        AppThemeChoice.Grape -> listOf(
            Color(0xFF756A86),
            Color(0xFF927384),
            Color(0xFFA98D9D),
            Color(0xFF7E8BA8),
            Color(0xFF9A7DA5),
            Color(0xFFAA7E8D),
            Color(0xFF847A9B)
        )
    }
}

private val WarmColorScheme = lightColorScheme(
    primary = Color(0xFF4F7E8A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCECEF),
    onPrimaryContainer = Color(0xFF19363D),
    secondary = Color(0xFF6D8F7E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3EEE8),
    onSecondaryContainer = Color(0xFF25382F),
    tertiary = Color(0xFFB08A68),
    onTertiary = Color.White,
    background = Color(0xFFFFF4E8),
    onBackground = Color(0xFF241F1B),
    surface = Color(0xFFFFFCF8),
    onSurface = Color(0xFF241F1B),
    surfaceVariant = Color(0xFFF2E0CE),
    onSurfaceVariant = Color(0xFF5A5148),
    outline = Color(0xFF8C7D70)
)

private val OceanColorScheme = lightColorScheme(
    primary = Color(0xFF4E7F92),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCECEF),
    onPrimaryContainer = Color(0xFF193742),
    secondary = Color(0xFF738AA0),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6EEF4),
    onSecondaryContainer = Color(0xFF283645),
    tertiary = Color(0xFF85A6B8),
    background = Color(0xFFEFF8FA),
    onBackground = Color(0xFF172126),
    surface = Color(0xFFFCFEFF),
    onSurface = Color(0xFF172126),
    surfaceVariant = Color(0xFFDDEBF0),
    onSurfaceVariant = Color(0xFF45545B),
    outline = Color(0xFF71828A)
)

private val ForestColorScheme = lightColorScheme(
    primary = Color(0xFF5E7D5B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE1EBDE),
    onPrimaryContainer = Color(0xFF253421),
    secondary = Color(0xFF7F8F66),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFECEFDF),
    onSecondaryContainer = Color(0xFF303823),
    tertiary = Color(0xFFA3A77A),
    background = Color(0xFFF2F7EA),
    onBackground = Color(0xFF202217),
    surface = Color(0xFFFEFFFA),
    onSurface = Color(0xFF202217),
    surfaceVariant = Color(0xFFE3EAD7),
    onSurfaceVariant = Color(0xFF555244),
    outline = Color(0xFF817C6B)
)

private val GrapeColorScheme = lightColorScheme(
    primary = Color(0xFF756A86),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEAE5F0),
    onPrimaryContainer = Color(0xFF302A3B),
    secondary = Color(0xFF927384),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF0E6EC),
    onSecondaryContainer = Color(0xFF3D2D37),
    tertiary = Color(0xFFA98D9D),
    background = Color(0xFFF7F0FA),
    onBackground = Color(0xFF251F24),
    surface = Color(0xFFFFFCFF),
    onSurface = Color(0xFF251F24),
    surfaceVariant = Color(0xFFECE1F1),
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
    var showBeadNumbers by remember { mutableStateOf(loadShowBeadNumbers(context)) }

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
                    HabitWidgetUpdater.updateAll(context)
                },
                showBeadNumbers = showBeadNumbers,
                onShowBeadNumbersChange = { showNumbers ->
                    showBeadNumbers = showNumbers
                    saveShowBeadNumbers(context, showNumbers)
                }
            )
        }
    }
}
