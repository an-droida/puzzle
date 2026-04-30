package com.games.puzzle.candycrush.feature.game.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.games.puzzle.candycrush.core.designsystem.colors.AppColors
import com.games.puzzle.candycrush.core.designsystem.components.AppButton

@Composable
fun RestartButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppButton(
        text = "Restart",
        onClick = onClick,
        modifier = modifier,
        containerColor = AppColors.CandyOrange,
        contentColor = AppColors.TextOnDark,
    )
}
