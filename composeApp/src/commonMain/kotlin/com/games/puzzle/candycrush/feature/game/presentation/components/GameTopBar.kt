package com.games.puzzle.candycrush.feature.game.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.games.puzzle.candycrush.core.designsystem.colors.AppColors
import com.games.puzzle.candycrush.core.designsystem.components.AppText
import com.games.puzzle.candycrush.core.designsystem.spacing.AppSpacing

@Composable
fun GameTopBar(
    onRestartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.TopBarBackground)
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppText(
            text = "Candy Crush",
            style = MaterialTheme.typography.titleLarge,
            color = AppColors.CandyYellow,
        )
        RestartButton(onClick = onRestartClick)
    }
}
