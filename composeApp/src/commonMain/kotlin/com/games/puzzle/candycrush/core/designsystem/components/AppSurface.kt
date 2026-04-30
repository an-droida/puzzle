package com.games.puzzle.candycrush.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.games.puzzle.candycrush.core.designsystem.colors.AppColors
import com.games.puzzle.candycrush.core.designsystem.spacing.AppSpacing

@Composable
fun AppSurface(
    modifier: Modifier = Modifier,
    color: Color = AppColors.BoardSurface,
    tonalElevation: Dp = 0.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(AppSpacing.cardRadius),
        color = color,
        tonalElevation = tonalElevation,
    ) {
        Box(content = content)
    }
}
