package info.sorbon.hskvocabulary.presentation.theme

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

private val LightColorScheme = lightColorScheme(
    primary = Blue700,
    onPrimary = Color.White,
    primaryContainer = Blue50,
    onPrimaryContainer = Blue900,
    secondary = Blue500,
    onSecondary = Color.White,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = Gray100,
    onSurfaceVariant = TextSecondary,
    background = BackgroundLight,
    onBackground = TextPrimary,
    error = Red500,
    onError = Color.White,
    outline = Color(0xFFCAC4D0)
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue700Dark,
    onPrimary = Color.White,
    primaryContainer = Blue200Dark,
    onPrimaryContainer = TextPrimaryDark,
    secondary = Blue400Dark,
    onSecondary = Color.White,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = Gray100Dark,
    onSurfaceVariant = TextSecondaryDark,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    error = Red500Dark,
    onError = Color.White,
    outline = Color(0xFF48484A)
)

@Composable
fun HskVocabularyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HskTypography,
        shapes = HskShapes,
        content = content
    )
}
