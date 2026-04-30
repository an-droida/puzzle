package com.games.puzzle.candycrush.feature.game.presentation.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.games.puzzle.candycrush.core.designsystem.colors.AppColors
import com.games.puzzle.candycrush.feature.game.domain.model.Candy
import com.games.puzzle.candycrush.feature.game.domain.model.CandyType
import com.games.puzzle.candycrush.feature.game.domain.model.Cell
import com.games.puzzle.candycrush.feature.game.presentation.animation.BoardAnimationState
import com.games.puzzle.candycrush.feature.game.presentation.animation.DragAxis
import com.games.puzzle.candycrush.feature.game.presentation.animation.DragState
import com.games.puzzle.candycrush.feature.game.presentation.gesture.SwipeDirection
import kotlin.math.abs
import kotlin.ranges.coerceIn
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@Composable
fun AnimatedCandyTile(
    candy: Candy,
    cell: Cell,
    cellSize: Dp,
    boardSize: Int,
    isSelected: Boolean,
    isBoardLocked: Boolean,
    animationState: BoardAnimationState,
    onClick: () -> Unit,
    onSwipe: (from: Cell, direction: SwipeDirection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val position = animationState.positionAnimatable(candy.id).value
    val alpha = animationState.alphaAnimatable(candy.id).value
    val baseScale = animationState.scaleAnimatable(candy.id).value
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

    // Selected pulse: infinite repeatable scale 1f -> 1.08f.
    val selectedPulse by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = if (isSelected) {
            infiniteRepeatable(
                animation = tween<Float>(durationMillis = 550),
                repeatMode = RepeatMode.Reverse,
            )
        } else {
            tween<Float>(durationMillis = 120)
        },
        label = "selected_pulse",
    )

    // Use animateFloatAsState for a quick selection ring fade.
    val selectionRingAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = 120),
        label = "selection_ring_alpha",
    )

    // Subtle padding change on selection (animateDpAsState).
    val cellPadding by animateDpAsState(
        targetValue = if (isSelected) 1.dp else 2.dp,
        animationSpec = tween(durationMillis = 120),
        label = "cell_padding",
    )

    // updateTransition example: animate selection ring width.
    val selectionTransition = updateTransition(targetState = isSelected, label = "selection_transition")
    val ringWidth by selectionTransition.animateDp(
        transitionSpec = { tween(durationMillis = 120) },
        label = "ring_width",
    ) { selected ->
        if (selected) 3.dp else 0.dp
    }

    val finalScale = baseScale * selectedPulse

    Box(
        modifier = modifier
            .size(cellSize)
            .graphicsLayer {
                translationX = position.x + visualDragOffset.x + visualShakeX
                translationY = position.y + visualDragOffset.y
                scaleX = finalScale
                scaleY = finalScale
                this.alpha = alpha
            }
            .padding(cellPadding)
            .clip(RoundedCornerShape(10.dp))
            .background(AppColors.CellBackground)
            .then(
                if (selectionRingAlpha > 0f) {
                    Modifier.border(
                        width = ringWidth,
                        color = AppColors.SelectionRing.copy(alpha = selectionRingAlpha),
                        shape = RoundedCornerShape(10.dp),
                    )
                }
                else Modifier,
            )
            // Tap handling (pointerInput as requested)
            .pointerInput(isBoardLocked, candy.id) {
                if (!isBoardLocked) {
                    detectTapGestures(
                        onTap = { onClick() },
                    )
                }
            }
            // Drag/swipe handling
            .pointerInput(isBoardLocked, candy.id) {
                if (isBoardLocked) return@pointerInput

                val cellSizePx = cellSize.toPx()
                val commitThresholdPx = cellSizePx * 0.35f
                val axisLockSlopPx = 6.dp.toPx()

                detectDragGestures(
                    onDragStart = {
                        suppressDragOverlay = false
                        // Stop any leftover shake from a previous invalid swap.
                        scope.launch { animationState.shakeAnimatable(candy.id).snapTo(0f) }
                        dragState = DragState(
                            candyId = candy.id,
                            fromCell = Cell(row = cell.row, col = cell.col, candy = null),
                            offset = Offset.Zero,
                            lockedAxis = null,
                            pendingDirection = null,
                        )
                    },
                    onDragCancel = {
                        suppressDragOverlay = false
                        dragState = null
                    },
                    onDragEnd = {
                        val pending = dragState
                        val direction = pending?.pendingDirection
                        if (pending != null && direction != null) {
                            // Commit drag offset -> base position and dispatch swipe ONLY on release.
                            suppressDragOverlay = true
                            // NOTE: must use the real dragOffset here. visualDragOffset becomes ZERO
                            // once we set suppressDragOverlay=true, which would cause a snap-back.
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
                            // Not far enough: animate back to the cell (keep drag state until finished).
                            dragState = pending.copy(offset = Offset.Zero, pendingDirection = null)
                            scope.launch {
                                delay(120)
                                dragState = null
                            }
                        } else {
                            suppressDragOverlay = false
                            dragState = null
                        }
                    },
                    onDrag = { change: PointerInputChange, dragAmount: Offset ->
                        change.consume()
                        val current = dragState ?: DragState(
                            candyId = candy.id,
                            fromCell = Cell(row = cell.row, col = cell.col, candy = null),
                            offset = Offset.Zero,
                            lockedAxis = null,
                            pendingDirection = null,
                        )

                        val raw = current.offset + dragAmount

                        // 1) Lock to dominant axis (once the gesture meaningfully starts).
                        val lockedAxis = current.lockedAxis ?: run {
                            val absX = abs(raw.x)
                            val absY = abs(raw.y)
                            when {
                                absX < axisLockSlopPx && absY < axisLockSlopPx -> null
                                absX >= absY -> DragAxis.Horizontal
                                else -> DragAxis.Vertical
                            }
                        }

                        // 2) Clamp to 1-cell distance and to board edges.
                        val maxPx = cellSizePx
                        val clamped = when (lockedAxis) {
                            DragAxis.Horizontal -> {
                                var x = raw.x.coerceIn(-maxPx, maxPx)
                                // Clamp invalid edge directions.
                                if (cell.col == 0) x = x.coerceIn(0f, maxPx)
                                if (cell.col == boardSize - 1) x = x.coerceIn(-maxPx, 0f)
                                Offset(x = x, y = 0f)
                            }

                            DragAxis.Vertical -> {
                                var y = raw.y.coerceIn(-maxPx, maxPx)
                                if (cell.row == 0) y = y.coerceIn(0f, maxPx)
                                if (cell.row == boardSize - 1) y = y.coerceIn(-maxPx, 0f)
                                Offset(x = 0f, y = y)
                            }

                            null -> Offset.Zero
                        }

                        // 3) Determine pending direction only when release should commit (>= 35% cell).
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
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        val color = candyColors2(candy.type)
//        val (dark, light) = candyColors(candy.type)
        Box(
            modifier = Modifier
                .fillMaxSize()
//                .padding(2.dp)
//                .shadow(
//                    elevation = if (isSelected) 8.dp else 4.dp,
//                    shape = CircleShape,
//                    ambientColor = dark.copy(alpha = 0.6f),
//                    spotColor = dark.copy(alpha = 0.6f),
//                )
//                .clip(CircleShape)
                .background(color),
//                .background(Brush.radialGradient(colors = listOf(light, dark))),
        ){
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(candy.type.icon), contentDescription = null
            )
        }
    }
}

private fun candyColors(type: CandyType): Pair<Color, Color> = when (type) {
    CandyType.RED -> AppColors.CandyRed to AppColors.CandyRedLight
    CandyType.ORANGE -> AppColors.CandyOrange to AppColors.CandyOrangeLight
    CandyType.YELLOW -> AppColors.CandyYellow to AppColors.CandyYellowLight
    CandyType.GREEN -> AppColors.CandyGreen to AppColors.CandyGreenLight
    CandyType.BLUE -> AppColors.CandyBlue to AppColors.CandyBlueLight
    CandyType.PURPLE -> AppColors.CandyPurple to AppColors.CandyPurpleLight
}
private fun candyColors2(type: CandyType): Color = when (type) {
    CandyType.RED ->  AppColors.CandyRed
    CandyType.ORANGE -> AppColors.CandyOrange
    CandyType.YELLOW -> AppColors.CandyYellow
    CandyType.GREEN -> AppColors.CandyGreen
    CandyType.BLUE ->  AppColors.CandyBlueLight
    CandyType.PURPLE ->  AppColors.CandyPurple
}


