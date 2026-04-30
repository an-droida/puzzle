package com.games.puzzle.candycrush.feature.game.presentation.components

import androidx.compose.animation.animateContentSize
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
fun ScoreBoard(
    score: Int,
    targetScore: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(AppColors.ScoreBackground, RoundedCornerShape(AppSpacing.cardRadius))
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppText(
            text = "SCORE",
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        AppText(
            text = score.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = AppColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        AppText(
            text = "/ $targetScore",
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}
