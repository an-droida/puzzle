package com.games.puzzle.candycrush.feature.game.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.games.puzzle.candycrush.core.designsystem.colors.AppColors
import com.games.puzzle.candycrush.core.designsystem.components.AppText
import com.games.puzzle.candycrush.core.designsystem.spacing.AppSpacing

@Composable
fun MovesCounter(
    movesRemaining: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(AppColors.MovesBackground, RoundedCornerShape(AppSpacing.cardRadius))
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppText(
            text = "MOVES",
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        AppText(
            text = movesRemaining.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = if (movesRemaining <= 5) AppColors.CandyRed else AppColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
    }
}
