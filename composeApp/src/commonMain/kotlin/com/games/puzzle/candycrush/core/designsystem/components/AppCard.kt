package com.games.puzzle.candycrush.core.designsystem.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.games.puzzle.candycrush.core.designsystem.colors.AppColors
import com.games.puzzle.candycrush.core.designsystem.spacing.AppSpacing

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    containerColor: Color = AppColors.BoardSurface,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(AppSpacing.cardRadius),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        content = content,
    )
}
