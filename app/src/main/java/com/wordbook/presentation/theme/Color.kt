package com.wordbook.presentation.theme

import androidx.compose.ui.graphics.Color

// Purple (default)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650A4)
val PurpleGrey40 = Color(0xFF625B71)
val Pink40 = Color(0xFF7D5260)

// Blue
val Blue80 = Color(0xFF90CAF9)
val Blue40 = Color(0xFF1565C0)

// Green
val Green80 = Color(0xFFA5D6A7)
val Green40 = Color(0xFF2E7D32)

// Orange
val Orange80 = Color(0xFFFFCC80)
val Orange40 = Color(0xFFE65100)

// Teal
val Teal80 = Color(0xFF80CBC4)
val Teal40 = Color(0xFF00695C)

// Red
val Red80 = Color(0xFFEF9A9A)
val Red40 = Color(0xFFC62828)

// Status colors
val ColorRemembered = Color(0xFF4CAF50)
val ColorNeedsReview = Color(0xFFF44336)
val ColorNotStudied = Color(0xFF9E9E9E)
val ColorCorrect = Color(0xFF43A047)
val ColorIncorrect = Color(0xFFE53935)
val ColorSkipped = Color(0xFF757575)

enum class AccentColor(
    val displayName: String,
    val light: Color,
    val dark: Color
) {
    Purple("Purple", Purple40, Purple80),
    Blue("Blue", Blue40, Blue80),
    Green("Green", Green40, Green80),
    Orange("Orange", Orange40, Orange80),
    Teal("Teal", Teal40, Teal80),
    Red("Red", Red40, Red80)
}
