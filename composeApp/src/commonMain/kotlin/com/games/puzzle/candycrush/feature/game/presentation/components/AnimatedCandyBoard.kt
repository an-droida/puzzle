package com.games.puzzle.candycrush.feature.game.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.min
import com.games.puzzle.candycrush.core.designsystem.colors.AppColors
import com.games.puzzle.candycrush.core.designsystem.spacing.AppSpacing
import com.games.puzzle.candycrush.feature.game.domain.model.Board
import com.games.puzzle.candycrush.feature.game.domain.model.CandyAnimationEvent
import com.games.puzzle.candycrush.feature.game.domain.model.Cell
import com.games.puzzle.candycrush.feature.game.presentation.animation.BoardAnimationState
import com.games.puzzle.candycrush.feature.game.presentation.gesture.SwipeDirection

@Composable
fun AnimatedCandyBoard(
    board: Board,
    isBoardLocked: Boolean,
    animationTurnId: Long,
    pendingAnimationEvents: List<CandyAnimationEvent>,
    onCandyClicked: (Cell) -> Unit,
    onCandySwiped: (from: Cell, direction: SwipeDirection) -> Unit,
    onAnimationsFinished: (turnId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        val boardSizeDp = min(maxWidth, maxHeight)

        Box(
            modifier = Modifier
                .size(boardSizeDp)
                .clip(RoundedCornerShape(AppSpacing.cardRadius))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(AppColors.BoardSurface, AppColors.BoardBackground),
                    ),
                )
                .padding(AppSpacing.boardPadding),
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val cellSizeDp = maxWidth / board.size
                val density = LocalDensity.current
                val cellSizePx = with(density) { cellSizeDp.toPx() }

                val animationState = remember { BoardAnimationState(board) }

                // Keep rendered board in sync when we're NOT animating.
                LaunchedEffect(board, cellSizePx, isBoardLocked) {
                    if (!isBoardLocked) {
                        animationState.syncToBoard(board, cellSizePx)
                    }
                }

                // Consume one-shot animation scripts.
                LaunchedEffect(animationTurnId) {
                    if (isBoardLocked && pendingAnimationEvents.isNotEmpty()) {
                        // Don't snap positions here. Drag/swipe may have already adjusted a candy's
                        // base position, and snapping would cause a visible reset before swap.
                        animationState.updateBoardWithoutSnapping(board)
                        animationState.play(pendingAnimationEvents, cellSizePx)
                        onAnimationsFinished(animationTurnId)
                    }
                }

                val renderedBoard = animationState.board

                Box(modifier = Modifier.fillMaxSize()) {
                    for (row in 0 until renderedBoard.size) {
                        for (col in 0 until renderedBoard.size) {
                            val cell = renderedBoard.getCell(row, col)
                            val candy = cell.candy ?: continue

                            androidx.compose.runtime.key(candy.id) {
                                AnimatedCandyTile(
                                    candy = candy,
                                    cell = cell,
                                    cellSize = cellSizeDp,
                                    boardSize = renderedBoard.size,
                                    isBoardLocked = isBoardLocked,
                                    animationState = animationState,
                                    onClick = {
                                        onCandyClicked(
                                            Cell(
                                                row = row,
                                                col = col,
                                                candy = null
                                            )
                                        )
                                    },
                                    onSwipe = { from, dir -> onCandySwiped(from, dir) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

