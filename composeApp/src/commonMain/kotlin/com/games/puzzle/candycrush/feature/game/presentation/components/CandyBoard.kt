package com.games.puzzle.candycrush.feature.game.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.min
import com.games.puzzle.candycrush.core.designsystem.colors.AppColors
import com.games.puzzle.candycrush.core.designsystem.spacing.AppSpacing
import com.games.puzzle.candycrush.feature.game.domain.model.Board

@Composable
fun CandyBoard(
    board: Board,
    selectedPosition: Pair<Int, Int>?,
    onCellClick: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        val boardSize = min(maxWidth, maxHeight)

        Box(
            modifier = Modifier
                .size(boardSize)
                .clip(RoundedCornerShape(AppSpacing.cardRadius))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(AppColors.BoardSurface, AppColors.BoardBackground),
                    ),
                )
                .padding(AppSpacing.boardPadding),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                for (row in 0 until board.size) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    ) {
                        for (col in 0 until board.size) {
                            val cell = board.getCell(row, col)
                            val isSelected = selectedPosition?.first == row &&
                                selectedPosition.second == col

                            CandyTile(
                                candy = cell.candy,
                                isSelected = isSelected,
                                onClick = { onCellClick(row, col) },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                            )
                        }
                    }
                }
            }
        }
    }
}
