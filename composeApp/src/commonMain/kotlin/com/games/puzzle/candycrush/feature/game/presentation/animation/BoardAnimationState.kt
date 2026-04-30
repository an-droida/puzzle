package com.games.puzzle.candycrush.feature.game.presentation.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.games.puzzle.candycrush.feature.game.domain.model.Board
import com.games.puzzle.candycrush.feature.game.domain.model.Candy
import com.games.puzzle.candycrush.feature.game.domain.model.CandyAnimationEvent
import com.games.puzzle.candycrush.feature.game.domain.model.CandyMove
import com.games.puzzle.candycrush.feature.game.domain.model.Cell
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Holds animation state for the board in the presentation layer.
 *
 * The important invariant is that candies are keyed by a stable [Candy.id].
 * That allows us to animate movement between cells while keeping Compose identity stable.
 */
class BoardAnimationState(
    initialBoard: Board,
) {

    var board: Board by mutableStateOf(initialBoard)
        private set

    private val positionByCandyId = mutableMapOf<Long, Animatable<Offset, AnimationVector2D>>()
    private val alphaByCandyId = mutableMapOf<Long, Animatable<Float, AnimationVector1D>>()
    private val scaleByCandyId = mutableMapOf<Long, Animatable<Float, AnimationVector1D>>()
    private val shakeByCandyId = mutableMapOf<Long, Animatable<Float, AnimationVector1D>>()

    // Visual-only flag required for better spawn control.
    private val isSpawningByCandyId = mutableStateMapOf<Long, Boolean>()

    fun isSpawning(candyId: Long): Boolean = isSpawningByCandyId[candyId] == true

    fun positionAnimatable(candyId: Long): Animatable<Offset, AnimationVector2D> =
        positionByCandyId.getOrPut(candyId) { Animatable(Offset.Zero, Offset.VectorConverter) }

    fun alphaAnimatable(candyId: Long): Animatable<Float, AnimationVector1D> =
        alphaByCandyId.getOrPut(candyId) { Animatable(1f) }

    fun scaleAnimatable(candyId: Long): Animatable<Float, AnimationVector1D> =
        scaleByCandyId.getOrPut(candyId) { Animatable(1f) }

    fun shakeAnimatable(candyId: Long): Animatable<Float, AnimationVector1D> =
        shakeByCandyId.getOrPut(candyId) { Animatable(0f) }

    suspend fun syncToBoard(newBoard: Board, cellSizePx: Float) {
        board = newBoard
        ensureStatesForCurrentBoard()
        snapAllToTargets(cellSizePx)
    }

    /**
     * Updates [board] and ensures animation state exists for all current candies, but does NOT
     * snap positions to cell targets.
     *
     * This is important for drag->swap handoff: the dragged candy may already be offset from its
     * cell origin and we want the swap animation to start from that exact on-screen position.
     */
    suspend fun updateBoardWithoutSnapping(newBoard: Board) {
        board = newBoard
        ensureStatesForCurrentBoard()
    }

    suspend fun play(events: List<CandyAnimationEvent>, cellSizePx: Float) {
        ensureStatesForCurrentBoard()

        for (event in events) {
            when (event) {
                is CandyAnimationEvent.Swap -> animateSwap(event.from, event.to, cellSizePx)
                is CandyAnimationEvent.InvalidSwap -> animateInvalidSwap(event.from, event.to, cellSizePx)
                is CandyAnimationEvent.Remove -> animateRemove(event.cells)
                is CandyAnimationEvent.Fall -> animateFall(event.moves, cellSizePx)
                is CandyAnimationEvent.Spawn -> animateSpawn(event.cells, cellSizePx)
                CandyAnimationEvent.BoardResolved -> Unit
            }
        }
    }

    private fun ensureStatesForCurrentBoard() {
        for (row in 0 until board.size) {
            for (col in 0 until board.size) {
                val candy = board.getCell(row, col).candy ?: continue
                positionAnimatable(candy.id)
                alphaAnimatable(candy.id)
                scaleAnimatable(candy.id)
                shakeAnimatable(candy.id)
            }
        }
    }

    private suspend fun snapAllToTargets(cellSizePx: Float) {
        for (row in 0 until board.size) {
            for (col in 0 until board.size) {
                val candy = board.getCell(row, col).candy ?: continue
                val target = cellOffsetPx(Cell(row = row, col = col, candy = null), cellSizePx)
                positionAnimatable(candy.id).snapTo(target)
                alphaAnimatable(candy.id).snapTo(1f)
                scaleAnimatable(candy.id).snapTo(1f)
                shakeAnimatable(candy.id).snapTo(0f)
            }
        }
    }

    private suspend fun animateSwap(from: Cell, to: Cell, cellSizePx: Float) {
        val fromCandy = board.getCell(from.row, from.col).candy ?: return
        val toCandy = board.getCell(to.row, to.col).candy ?: return

        // Update the rendered board immediately; Animatables still hold old offsets.
        board = swapCells(board, from, to)

        coroutineScope {
            launch {
                positionAnimatable(fromCandy.id).animateTo(
                    targetValue = cellOffsetPx(to, cellSizePx),
                    animationSpec = tween(durationMillis = 180),
                )
            }
            launch {
                positionAnimatable(toCandy.id).animateTo(
                    targetValue = cellOffsetPx(from, cellSizePx),
                    animationSpec = tween(durationMillis = 180),
                )
            }
        }
    }

    private suspend fun animateInvalidSwap(from: Cell, to: Cell, cellSizePx: Float) {
        val fromCandy = board.getCell(from.row, from.col).candy ?: return
        val toCandy = board.getCell(to.row, to.col).candy ?: return

        // There
        board = swapCells(board, from, to)
        coroutineScope {
            launch {
                positionAnimatable(fromCandy.id).animateTo(
                    targetValue = cellOffsetPx(to, cellSizePx),
                    animationSpec = tween(durationMillis = 120),
                )
            }
            launch {
                positionAnimatable(toCandy.id).animateTo(
                    targetValue = cellOffsetPx(from, cellSizePx),
                    animationSpec = tween(durationMillis = 120),
                )
            }
        }

        // Back
        board = swapCells(board, from, to)
        coroutineScope {
            launch {
                positionAnimatable(fromCandy.id).animateTo(
                    targetValue = cellOffsetPx(from, cellSizePx),
                    animationSpec = tween(durationMillis = 120),
                )
            }
            launch {
                positionAnimatable(toCandy.id).animateTo(
                    targetValue = cellOffsetPx(to, cellSizePx),
                    animationSpec = tween(durationMillis = 120),
                )
            }
        }

        animateShake(listOf(fromCandy.id, toCandy.id))
    }

    private suspend fun animateRemove(cells: Set<Cell>) {
        // Animate alpha/scale down, then actually remove candies from the rendered board.
        val idsToRemove = cells.mapNotNull { cell -> board.getCell(cell.row, cell.col).candy?.id }
        if (idsToRemove.isEmpty()) return

        coroutineScope {
            idsToRemove.flatMap { id ->
                listOf(
                    launch { alphaAnimatable(id).animateTo(0f, tween(durationMillis = 160)) },
                    launch { scaleAnimatable(id).animateTo(0.2f, tween(durationMillis = 160)) },
                )
            }.joinAll()
        }

        var newBoard = board
        for (cell in cells) {
            if (newBoard.isValidPosition(cell.row, cell.col)) {
                newBoard = newBoard.withCell(cell.row, cell.col, null)
            }
        }
        board = newBoard

        // Remove visual state for deleted candies (optional cleanup).
        for (id in idsToRemove) {
            positionByCandyId.remove(id)
            alphaByCandyId.remove(id)
            scaleByCandyId.remove(id)
            shakeByCandyId.remove(id)
        }

        // Ensure remaining states are still consistent.
        ensureStatesForCurrentBoard()
    }

    private suspend fun animateFall(moves: List<CandyMove>, cellSizePx: Float) {
        if (moves.isEmpty()) return

        // Apply board updates first, then animate positions to new targets.
        board = applyMoves(board, moves)

        coroutineScope {
            moves.map { move ->
                launch {
                    val distance = abs(move.from.row - move.to.row) + abs(move.from.col - move.to.col)
                    val duration = (220 + (distance - 1).coerceAtLeast(0) * 30).coerceAtMost(320)
                    positionAnimatable(move.candyId).animateTo(
                        targetValue = cellOffsetPx(move.to, cellSizePx),
                        animationSpec = tween(durationMillis = duration),
                    )
                }
            }.joinAll()
        }
    }

    private suspend fun animateSpawn(cells: List<Cell>, cellSizePx: Float) {
        if (cells.isEmpty()) return

        // --- Phase 1: prepare visual state BEFORE inserting into the rendered board ---
        // This prevents a 1-frame flash where the candy appears at its final cell.
        val spawned = cells.mapNotNull { cell ->
            val candy = cell.candy ?: return@mapNotNull null
            candy.id to cell
        }
        if (spawned.isEmpty()) return

        // Determine a per-column spawn index to stack start positions (multiple new candies in one column).
        val spawnIndexById = buildMap<Long, Int> {
            spawned
                .groupBy { (_, cell) -> cell.col }
                .forEach { (_, list) ->
                    val sorted = list.sortedBy { it.second.row }
                    sorted.forEachIndexed { index, (id, _) ->
                        put(id, index)
                    }
                }
        }

        // Initialize animatables and spawning flags.
        for ((id, cell) in spawned) {
            isSpawningByCandyId[id] = true
            val indexInColumn = spawnIndexById[id] ?: 0
            val start = Offset(
                x = cell.col * cellSizePx,
                y = -cellSizePx * (indexInColumn + 1),
            )
            positionAnimatable(id).snapTo(start)
            alphaAnimatable(id).snapTo(0f)
            scaleAnimatable(id).snapTo(0.6f)
            shakeAnimatable(id).snapTo(0f)
        }

        // Now insert candies into the rendered board.
        var newBoard = board
        for ((_, cell) in spawned) {
            val candy = cell.candy ?: continue
            newBoard = newBoard.withCell(cell.row, cell.col, candy)
        }
        board = newBoard

        // --- Phase 2: animate fall + fade + scale in (with stagger) ---
        coroutineScope {
            spawned.map { (id, cell) ->
                launch {
                    val indexInColumn = spawnIndexById[id] ?: 0
                    val staggerMs = (indexInColumn * 35) + (cell.col * 12)
                    delay(staggerMs.toLong())

                    val target = cellOffsetPx(cell, cellSizePx)
                    // Animate together for a smooth "fall in".
                    launch {
                        positionAnimatable(id).animateTo(
                            targetValue = target,
                            animationSpec = tween(durationMillis = 220, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                        )
                    }
                    launch {
                        alphaAnimatable(id).animateTo(
                            targetValue = 1f,
                            animationSpec = tween(durationMillis = 200),
                        )
                    }
                    launch {
                        scaleAnimatable(id).animateTo(
                            targetValue = 1f,
                            animationSpec = tween(durationMillis = 200, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                        )
                    }
                }
            }.joinAll()
        }

        // Mark spawning finished.
        for ((id, _) in spawned) {
            isSpawningByCandyId[id] = false
        }
    }

    private suspend fun animateShake(candyIds: List<Long>) {
        coroutineScope {
            candyIds.map { id ->
                launch {
                    shakeAnimatable(id).animateTo(
                        targetValue = 0f,
                        animationSpec = keyframes {
                            durationMillis = 260
                            0f at 0
                            8f at 50
                            (-8f) at 100
                            6f at 145
                            (-6f) at 185
                            3f at 220
                            (-3f) at 240
                            0f at 260
                        },
                    )
                }
            }.joinAll()
        }
    }

    private fun cellOffsetPx(cell: Cell, cellSizePx: Float): Offset =
        Offset(
            x = (cell.col * cellSizePx).roundToInt().toFloat(),
            y = (cell.row * cellSizePx).roundToInt().toFloat(),
        )

    private fun swapCells(board: Board, a: Cell, b: Cell): Board {
        val candyA = board.getCell(a.row, a.col).candy
        val candyB = board.getCell(b.row, b.col).candy
        return board
            .withCell(a.row, a.col, candyB)
            .withCell(b.row, b.col, candyA)
    }

    private fun applyMoves(board: Board, moves: List<CandyMove>): Board {
        val size = board.size
        val candies = Array(size) { row -> Array<Candy?>(size) { col -> board.getCell(row, col).candy } }
        val candyById = buildMap<Long, Candy> {
            for (row in 0 until size) {
                for (col in 0 until size) {
                    val candy = candies[row][col] ?: continue
                    put(candy.id, candy)
                }
            }
        }

        // IMPORTANT: do NOT apply (from -> null, to -> candy) in a single pass.
        // In gravity, a destination cell can also be another candy's source.
        // If we write to 'to' and later clear that same coordinate as a 'from',
        // the candy will disappear for a frame (or be lost entirely).
        // Two-pass update avoids this ordering hazard.
        for (move in moves) {
            candies[move.from.row][move.from.col] = null
        }
        for (move in moves) {
            candies[move.to.row][move.to.col] = candyById[move.candyId]
        }

        var result = board
        for (row in 0 until size) {
            for (col in 0 until size) {
                result = result.withCell(row, col, candies[row][col])
            }
        }
        return result
    }
}

