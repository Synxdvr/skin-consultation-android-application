package com.example.skinconsultform.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

// ── Light color scheme (spa always uses light — no dark mode needed) ──
private val StheticLightColorScheme = lightColorScheme(

    primary          = StheticColors.Gold500,
    onPrimary        = StheticColors.White,
    primaryContainer = StheticColors.Gold100,
    onPrimaryContainer = StheticColors.Gold900,

    secondary        = StheticColors.Rose500,
    onSecondary      = StheticColors.White,
    secondaryContainer = StheticColors.Rose100,
    onSecondaryContainer = StheticColors.Rose500,

    tertiary         = StheticColors.Sage500,
    onTertiary       = StheticColors.White,
    tertiaryContainer = StheticColors.Sage100,
    onTertiaryContainer = StheticColors.Sage500,

    background       = StheticColors.Cream50,
    onBackground     = StheticColors.Charcoal900,

    surface          = StheticColors.Cream100,
    onSurface        = StheticColors.Charcoal900,
    surfaceVariant   = StheticColors.Cream200,
    onSurfaceVariant = StheticColors.Charcoal700,

    outline          = StheticColors.Cream300,
    outlineVariant   = StheticColors.Gold300,

    error            = StheticColors.Error,
    onError          = StheticColors.White,
    errorContainer   = StheticColors.ErrorLight,
    onErrorContainer = StheticColors.Error
)

// ── Composition local for extended colors ─────────────────────────────
data class StheticExtendedColors(
    val goldDivider: Color,
    val inputBackground: Color,
    val chipSelected: Color,
    val chipUnselected: Color,
    val chipSelectedText: Color,
    val chipUnselectedText: Color,
    val stepActive: Color,
    val stepComplete: Color,
    val stepInactive: Color,
    val warningBackground: Color,
    val successBackground: Color
)

val LocalStheticColors = staticCompositionLocalOf {
    StheticExtendedColors(
        goldDivider        = StheticColors.Gold300,
        inputBackground    = StheticColors.Cream200,
        chipSelected       = StheticColors.Gold500,
        chipUnselected     = StheticColors.Cream200,
        chipSelectedText   = StheticColors.White,
        chipUnselectedText = StheticColors.Charcoal700,
        stepActive         = StheticColors.Gold500,
        stepComplete       = StheticColors.Gold700,
        stepInactive       = StheticColors.Cream300,
        warningBackground  = StheticColors.WarningLight,
        successBackground  = StheticColors.SuccessLight
    )
}

@Composable
fun StheticTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalStheticColors provides StheticExtendedColors(
            goldDivider        = StheticColors.Gold300,
            inputBackground    = StheticColors.Cream200,
            chipSelected       = StheticColors.Gold500,
            chipUnselected     = StheticColors.Cream200,
            chipSelectedText   = StheticColors.White,
            chipUnselectedText = StheticColors.Charcoal700,
            stepActive         = StheticColors.Gold500,
            stepComplete       = StheticColors.Gold700,
            stepInactive       = StheticColors.Cream300,
            warningBackground  = StheticColors.WarningLight,
            successBackground  = StheticColors.SuccessLight
        )
    ) {
        MaterialTheme(
            colorScheme = StheticLightColorScheme,
            typography  = StheticTypography,
            content     = content
        )
    }
}

// ── Convenience accessor ──────────────────────────────────────────────
object StheticTheme {
    val extendedColors: StheticExtendedColors
        @Composable get() = LocalStheticColors.current
}