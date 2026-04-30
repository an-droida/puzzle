package com.games.puzzle.candycrush.feature.game.domain.usecase

import com.games.puzzle.candycrush.feature.game.domain.model.Board
import com.games.puzzle.candycrush.feature.game.domain.model.Cell

class ApplyGravityUseCase {
    operator fun invoke(board: Board): Board {
        val size = board.size
        val newCells = Array(size) { row -> Array(size) { col -> board.getCell(row, col) } }

        for (col in 0 until size) {
            // Collect non-null candies from bottom to top
            val candies = (size - 1 downTo 0)
                .mapNotNull { row -> newCells[row][col].candy }

            // Place them at the bottom, fill top with nulls
            for (row in size - 1 downTo 0) {
                val candyIndex = (size - 1) - row
                newCells[row][col] = Cell(
                    row = row,
                    col = col,
                    candy = if (candyIndex < candies.size) candies[candyIndex] else null,
                )
            }
        }

        return board.copy(cells = newCells.map { it.toList() })
    }
}
