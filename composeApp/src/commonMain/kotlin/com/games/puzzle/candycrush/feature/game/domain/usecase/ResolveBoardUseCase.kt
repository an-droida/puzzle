package com.games.puzzle.candycrush.feature.game.domain.usecase

import com.games.puzzle.candycrush.feature.game.domain.model.Board

class ResolveBoardUseCase(
    private val detectMatchesUseCase: DetectMatchesUseCase,
    private val applyGravityUseCase: ApplyGravityUseCase,
    private val refillBoardUseCase: RefillBoardUseCase,
) {
    data class Result(val board: Board, val pointsEarned: Int)

    operator fun invoke(
        board: Board,
        pointsPerCandy: Int,
        idCounter: () -> Long,
    ): Result {
        var current = board
        var totalPoints = 0

        while (true) {
            val matches = detectMatchesUseCase(current)
            if (matches.isEmpty()) break

            totalPoints += matches.size * pointsPerCandy

            // Remove matched candies
            for ((row, col) in matches) {
                current = current.withCell(row, col, null)
            }

            current = applyGravityUseCase(current)
            current = refillBoardUseCase(current, idCounter)
        }

        return Result(board = current, pointsEarned = totalPoints)
    }
}
