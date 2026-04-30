package com.games.puzzle.candycrush.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import com.games.puzzle.candycrush.core.designsystem.colors.AppColors
import com.games.puzzle.candycrush.core.designsystem.typography.AppTypography

private val AppColorScheme = darkColorScheme(
    primary = AppColors.CandyPurple,
    onPrimary = AppColors.TextOnDark,
    primaryContainer = AppColors.CandyPurpleLight,
    secondary = AppColors.CandyBlue,
    onSecondary = AppColors.TextOnDark,
    secondaryContainer = AppColors.CandyBlueLight,
    tertiary = AppColors.CandyGreen,
    background = AppColors.BoardBackground,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.BoardSurface,
    onSurface = AppColors.TextPrimary,
    error = AppColors.CandyRed,
    onError = AppColors.TextOnDark,
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = AppTypography,
        content = content,
    )
}
