package com.games.puzzle.candycrush.feature.game.domain.usecase

import com.games.puzzle.candycrush.feature.game.domain.model.Board

class DetectMatchesUseCase {
    operator fun invoke(board: Board): Set<Pair<Int, Int>> {
        val matched = mutableSetOf<Pair<Int, Int>>()
        val size = board.size

        // Horizontal matches
        for (row in 0 until size) {
            var col = 0
            while (col < size) {
                val type = board.getCell(row, col).candy?.type ?: run { col++; continue }
                var end = col + 1
                while (end < size && board.getCell(row, end).candy?.type == type) end++
                if (end - col >= 3) {
                    for (c in col until end) matched.add(row to c)
                }
                col = end
            }
        }

        // Vertical matches
        for (col in 0 until size) {
            var row = 0
            while (row < size) {
                val type = board.getCell(row, col).candy?.type ?: run { row++; continue }
                var end = row + 1
                while (end < size && board.getCell(end, col).candy?.type == type) end++
                if (end - row >= 3) {
                    for (r in row until end) matched.add(r to col)
                }
                row = end
            }
        }

        return matched
    }
}
