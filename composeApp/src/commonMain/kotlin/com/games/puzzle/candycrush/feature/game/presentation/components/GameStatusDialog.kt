package com.games.puzzle.candycrush.feature.game.presentation.components

import androidx.compose.runtime.Composable
import com.games.puzzle.candycrush.core.designsystem.colors.AppColors
import com.games.puzzle.candycrush.core.designsystem.components.AppDialog
import com.games.puzzle.candycrush.feature.game.domain.model.GameStatus

@Composable
fun GameStatusDialog(
    status: GameStatus,
    score: Int,
    targetScore: Int,
    onRestart: () -> Unit,
) {
    when (status) {
        is GameStatus.Won -> AppDialog(
            title = "You Won! 🎉",
            message = "Amazing! You reached $score points!\nTarget was $targetScore.",
            primaryButtonText = "Play Again",
            onPrimaryClick = onRestart,
            containerColor = AppColors.WinBackground,
        )
        is GameStatus.Lost -> AppDialog(
            title = "Game Over 😢",
            message = "You scored $score points.\nTarget was $targetScore.",
            primaryButtonText = "Try Again",
            onPrimaryClick = onRestart,
            containerColor = AppColors.LossBackground,
        )
        is GameStatus.Playing -> Unit
    }
}
