package info.sorbon.hskvocabulary.presentation.theme

import androidx.compose.ui.graphics.Color

// Blue scale (matching iOS UIColor+Ext asset catalog)
val Blue50 = Color(0xFFE0F4FD)
val Blue100 = Color(0xFFB1E3FA)
val Blue200 = Color(0xFF7CD1F7)
val Blue300 = Color(0xFF46BFF4)
val Blue400 = Color(0xFF0FB2F1)
val Blue500 = Color(0xFF00A4EF)
val Blue600 = Color(0xFF0096E0)
val Blue700 = Color(0xFF0073B9)
val Blue800 = Color(0xFF0073B9)
val Blue900 = Color(0xFF005397)

// Dark mode blue overrides (matching iOS asset catalog)
val Blue50Dark = Color(0xFF0A2233)
val Blue100Dark = Color(0xFF0D3550)
val Blue200Dark = Color(0xFF00527A)
val Blue300Dark = Color(0xFF0E7AB5)
val Blue400Dark = Color(0xFF00D0D0)
val Blue500Dark = Color(0xFF00A4EF)
val Blue600Dark = Color(0xFF0096E0)
val Blue700Dark = Color(0xFF0A84FF)
val Blue800Dark = Color(0xFF0A84FF)
val Blue900Dark = Color(0xFF006FD6)

// Semantic colors
val Red500 = Color(0xFFDE2525)
val Red800 = Color(0xFFCB3234)
val Red500Dark = Color(0xFFFF453A)
val Red800Dark = Color(0xFFD70015)

val Green500 = Color(0xFF52BA2F)
val Green600 = Color(0xFF3D9B1E)
val Green500Dark = Color(0xFF30D158)
val Green600Dark = Color(0xFF25A244)

val StarColor = Color(0xFFFFD52C)
val StarColorDark = Color(0xFFFFD60A)

// Grays
val Gray100 = Color(0xFFF5F5F5)
val Gray200 = Color(0xFFEEEEEE)
val Gray100Dark = Color(0xFF3A3A3C)
val Gray200Dark = Color(0xFF2E2E30)

// Text
val TextPrimary = Color(0xFF313135)
val TextSecondary = Color(0xFF686D73)
val TextDisabled = Color(0xFF969696)
val TextPrimaryDark = Color(0xFFF2F2F7)
val TextSecondaryDark = Color(0xFF8E8E93)
val TextDisabledDark = Color(0xFF636366)

// Background/Surface (from iOS asset catalog)
val BackgroundLight = Color(0xFFF5F5F5)
val SurfaceLight = Color(0xFFFFFFFF)
val BackgroundDark = Color(0xFF1C1C1E)
val SurfaceDark = Color(0xFF2C2C2E)

// HSK Level colors (maps to iOS HSKEnum backgroundColor: blue300..blue800)
val HskLevelColors = listOf(Blue300, Blue400, Blue500, Blue600, Blue700, Blue800)
