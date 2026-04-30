package com.games.puzzle.candycrush.feature.game.domain.usecase

import com.games.puzzle.candycrush.feature.game.domain.model.CandyAnimationEvent
import com.games.puzzle.candycrush.feature.game.domain.model.CandyMove
import com.games.puzzle.candycrush.feature.game.domain.model.Board
import com.games.puzzle.candycrush.feature.game.domain.model.Cell

class ResolveBoardUseCase(
    private val detectMatchesUseCase: DetectMatchesUseCase,
    private val applyGravityUseCase: ApplyGravityUseCase,
    private val refillBoardUseCase: RefillBoardUseCase,
) {
    data class Result(
        val board: Board,
        val pointsEarned: Int,
        val events: List<CandyAnimationEvent>,
    )

    operator fun invoke(
        board: Board,
        pointsPerCandy: Int,
        idCounter: () -> Long,
    ): Result {
        var current = board
        var totalPoints = 0
        val events = mutableListOf<CandyAnimationEvent>()

        while (true) {
            val matches = detectMatchesUseCase(current)
            if (matches.isEmpty()) break

            totalPoints += matches.size * pointsPerCandy

            // 1) Remove
            val removeCells = matches
                .map { (row, col) -> Cell(row = row, col = col, candy = null) }
                .toSet()
            events += CandyAnimationEvent.Remove(cells = removeCells)

            for ((row, col) in matches) current = current.withCell(row, col, null)

            // 2) Fall (gravity)
            val beforeGravity = current
            val afterGravity = applyGravityUseCase(current)
            val fallMoves = computeMoves(before = beforeGravity, after = afterGravity)
            if (fallMoves.isNotEmpty()) {
                events += CandyAnimationEvent.Fall(moves = fallMoves)
            }
            current = afterGravity

            // 3) Spawn (refill)
            val beforeRefill = current
            val afterRefill = refillBoardUseCase(current, idCounter)
            val spawnedCells = computeSpawnedCells(before = beforeRefill, after = afterRefill)
            if (spawnedCells.isNotEmpty()) {
                events += CandyAnimationEvent.Spawn(cells = spawnedCells)
            }
            current = afterRefill
        }

        events += CandyAnimationEvent.BoardResolved
        return Result(board = current, pointsEarned = totalPoints, events = events)
    }

    private fun computeMoves(before: Board, after: Board): List<CandyMove> {
        val beforePositions = buildMap<Long, Cell> {
            for (row in 0 until before.size) {
                for (col in 0 until before.size) {
                    val candy = before.getCell(row, col).candy ?: continue
                    put(candy.id, Cell(row = row, col = col, candy = null))
                }
            }
        }

        val moves = mutableListOf<CandyMove>()
        for (row in 0 until after.size) {
            for (col in 0 until after.size) {
                val candy = after.getCell(row, col).candy ?: continue
                val from = beforePositions[candy.id] ?: continue // new candy created later (refill)
                val to = Cell(row = row, col = col, candy = null)
                if (from.row != to.row || from.col != to.col) {
                    moves += CandyMove(candyId = candy.id, from = from, to = to)
                }
            }
        }
        return moves
    }

    private fun computeSpawnedCells(before: Board, after: Board): List<Cell> {
        val beforeIds = buildSet<Long> {
            for (row in 0 until before.size) {
                for (col in 0 until before.size) {
                    val candy = before.getCell(row, col).candy ?: continue
                    add(candy.id)
                }
            }
        }

        val spawned = mutableListOf<Cell>()
        for (row in 0 until after.size) {
            for (col in 0 until after.size) {
                val cell = after.getCell(row, col)
                val candy = cell.candy ?: continue
                if (candy.id !in beforeIds) {
                    spawned += cell
                }
            }
        }
        return spawned
    }
}
