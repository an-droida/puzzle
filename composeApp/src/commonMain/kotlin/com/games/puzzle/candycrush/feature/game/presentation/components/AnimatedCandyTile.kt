package com.games.puzzle.candycrush.feature.game.presentation.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.games.puzzle.candycrush.core.designsystem.colors.AppColors
import com.games.puzzle.candycrush.core.designsystem.spacing.AppSpacing.candyGap
import com.games.puzzle.candycrush.core.designsystem.spacing.AppSpacing.candyTileRadius
import com.games.puzzle.candycrush.feature.game.domain.model.Candy
import com.games.puzzle.candycrush.feature.game.domain.model.CandyType
import com.games.puzzle.candycrush.feature.game.domain.model.Cell
import com.games.puzzle.candycrush.feature.game.presentation.animation.BoardAnimationState
import com.games.puzzle.candycrush.feature.game.presentation.animation.DragAxis
import com.games.puzzle.candycrush.feature.game.presentation.animation.DragState
import com.games.puzzle.candycrush.feature.game.presentation.gesture.SwipeDirection
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.abs

@Composable
fun AnimatedCandyTile(
    candy: Candy,
    cell: Cell,
    cellSize: Dp,
    boardSize: Int,
    isBoardLocked: Boolean,
    animationState: BoardAnimationState,
    onClick: () -> Unit,
    onSwipe: (from: Cell, direction: SwipeDirection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val position = animationState.positionAnimatable(candy.id).value
    val alpha = animationState.alphaAnimatable(candy.id).value
    val shakeX = animationState.shakeAnimatable(candy.id).value

    val scope = rememberCoroutineScope()

    var dragState by remember(candy.id) { mutableStateOf<DragState?>(null) }
    var suppressDragOverlay by remember(candy.id) { mutableStateOf(false) }
    val rawDragOffset = dragState?.offset ?: Offset.Zero

    // Smoothly follow the pointer to avoid jittery drag movement.
    // Using a non-bouncy spring keeps it responsive without overshoot.
    val dragOffset by animateValueAsState(
        targetValue = rawDragOffset,
        typeConverter = Offset.VectorConverter,
        animationSpec = tween<Offset>(
            durationMillis = 80,
            easing = LinearOutSlowInEasing,
        ),
        label = "drag_offset",
    )

    // During the drag->swap handoff we must prevent applying dragOffset on top of the committed base position.
    val visualDragOffset = if (suppressDragOverlay) Offset.Zero else dragOffset

    // Avoid mixing invalid-swap shake with drag-follow (feels like unwanted shaking).
    val visualShakeX = if (dragState != null) 0f else shakeX

    Box(
        modifier = modifier
            .size(cellSize)
            .graphicsLayer {
                translationX = position.x + visualDragOffset.x + visualShakeX
                translationY = position.y + visualDragOffset.y
                this.alpha = alpha
            }
            .padding(candyGap)
            .clip(RoundedCornerShape(candyTileRadius))
            .background(AppColors.CellBackground)
            .pointerInput(isBoardLocked, candy.id, boardSize) {
                if (isBoardLocked) return@pointerInput

                val cellSizePx = cellSize.toPx()
                val commitThresholdPx = cellSizePx * 0.35f
                val axisLockSlopPx = 4.dp.toPx()

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)

                    suppressDragOverlay = false
                    scope.launch { animationState.shakeAnimatable(candy.id).snapTo(0f) }

                    dragState = DragState(
                        candyId = candy.id,
                        fromCell = Cell(row = cell.row, col = cell.col, candy = null),
                        offset = Offset.Zero,
                        lockedAxis = null,
                        pendingDirection = null,
                    )

                    var totalDelta = Offset.Zero
                    var rawAccumulated = Offset.Zero

                    while (true) {
                        val event = awaitPointerEvent()
                        val change: PointerInputChange =
                            event.changes.firstOrNull { it.id == down.id } ?: break

                        if (!change.pressed) break

                        val delta = change.positionChange()
                        if (delta != Offset.Zero) {
                            totalDelta += delta
                            rawAccumulated += delta
                            change.consume()

                            val current = dragState ?: DragState(
                                candyId = candy.id,
                                fromCell = Cell(row = cell.row, col = cell.col, candy = null),
                                offset = Offset.Zero,
                                lockedAxis = null,
                                pendingDirection = null,
                            )

                            // 1) Lock to dominant axis (based on accumulated movement).
                            val lockedAxis = current.lockedAxis ?: run {
                                val absX = abs(rawAccumulated.x)
                                val absY = abs(rawAccumulated.y)
                                when {
                                    absX < axisLockSlopPx && absY < axisLockSlopPx -> null
                                    absX >= absY -> DragAxis.Horizontal
                                    else -> DragAxis.Vertical
                                }
                            }

                            // 2) Clamp to 1-cell and board edges.
                            val maxPx = cellSizePx
                            val clamped = when (lockedAxis) {
                                DragAxis.Horizontal -> {
                                    var x = rawAccumulated.x.coerceIn(-maxPx, maxPx)
                                    if (cell.col == 0) x = x.coerceIn(0f, maxPx)
                                    if (cell.col == boardSize - 1) x = x.coerceIn(-maxPx, 0f)
                                    Offset(x = x, y = 0f)
                                }

                                DragAxis.Vertical -> {
                                    var y = rawAccumulated.y.coerceIn(-maxPx, maxPx)
                                    if (cell.row == 0) y = y.coerceIn(0f, maxPx)
                                    if (cell.row == boardSize - 1) y = y.coerceIn(-maxPx, 0f)
                                    Offset(x = 0f, y = y)
                                }

                                null -> Offset.Zero
                            }

                            // Keep the accumulated raw offset consistent with the clamped offset so
                            // reversing direction doesn't require "undoing" out-of-bounds deltas.
                            rawAccumulated = when (lockedAxis) {
                                DragAxis.Horizontal -> Offset(x = clamped.x, y = 0f)
                                DragAxis.Vertical -> Offset(x = 0f, y = clamped.y)
                                null -> rawAccumulated
                            }

                            // 3) Pending direction when past 35%.
                            val direction = when (lockedAxis) {
                                DragAxis.Horizontal -> when {
                                    abs(clamped.x) >= commitThresholdPx && clamped.x > 0f -> SwipeDirection.RIGHT
                                    abs(clamped.x) >= commitThresholdPx && clamped.x < 0f -> SwipeDirection.LEFT
                                    else -> null
                                }

                                DragAxis.Vertical -> when {
                                    abs(clamped.y) >= commitThresholdPx && clamped.y > 0f -> SwipeDirection.DOWN
                                    abs(clamped.y) >= commitThresholdPx && clamped.y < 0f -> SwipeDirection.UP
                                    else -> null
                                }

                                null -> null
                            }

                            dragState = current.copy(
                                offset = clamped,
                                lockedAxis = lockedAxis,
                                pendingDirection = direction,
                            )
                        }
                    }

                    // Release.
                    val pending = dragState
                    val direction = pending?.pendingDirection
                    val wasDragged =
                        abs(totalDelta.x) > axisLockSlopPx || abs(totalDelta.y) > axisLockSlopPx

                    if (!wasDragged) {
                        dragState = null
                        onClick()
                    } else if (pending != null && direction != null) {
                        suppressDragOverlay = true
                        val commitOffset = dragOffset
                        val fromCell = pending.fromCell
                        scope.launch(start = CoroutineStart.UNDISPATCHED) {
                            val anim = animationState.positionAnimatable(candy.id)
                            anim.snapTo(anim.value + commitOffset)

                            dragState = null
                            onSwipe(fromCell, direction)

                            delay(140)
                            suppressDragOverlay = false
                        }
                    } else if (pending != null && pending.offset != Offset.Zero) {
                        dragState = pending.copy(offset = Offset.Zero, pendingDirection = null)
                        scope.launch {
                            delay(160)
                            dragState = null
                        }
                    } else {
                        dragState = null
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(candyColors2(candy.type)),
        ) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(candy.type.icon), contentDescription = null
            )
        }
    }
}

private fun candyColors2(type: CandyType): Color = when (type) {
    CandyType.RED -> AppColors.CandyRed
    CandyType.ORANGE -> AppColors.CandyOrange
    CandyType.YELLOW -> AppColors.CandyYellow
    CandyType.GREEN -> AppColors.CandyGreen
    CandyType.BLUE -> AppColors.CandyBlueLight
    CandyType.PURPLE -> AppColors.CandyPurple
}


