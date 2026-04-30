package com.games.puzzle.candycrush.feature.game.domain.usecase

import com.games.puzzle.candycrush.feature.game.domain.model.Board
import com.games.puzzle.candycrush.feature.game.domain.model.Candy
import com.games.puzzle.candycrush.feature.game.domain.model.CandyType
import com.games.puzzle.candycrush.feature.game.domain.model.Cell
import com.games.puzzle.candycrush.feature.game.domain.model.GameConfig
import com.games.puzzle.candycrush.feature.game.domain.model.GameState
import com.games.puzzle.candycrush.feature.game.domain.model.GameStatus

class CreateNewGameUseCase {
    operator fun invoke(config: GameConfig, idCounter: () -> Long): GameState {
        val board = buildBoardWithoutInitialMatches(config, idCounter)
        return GameState(
            board = board,
            score = 0,
            movesRemaining = config.maxMoves,
            selectedCell = null,
            status = GameStatus.Playing,
            config = config,
        )
    }

    private fun buildBoardWithoutInitialMatches(config: GameConfig, idCounter: () -> Long): Board {
        val size = config.boardSize
        val cells = Array(size) { row ->
            Array(size) { col ->
                Cell(row = row, col = col, candy = null)
            }
        }

        for (row in 0 until size) {
            for (col in 0 until size) {
                val forbidden = mutableSetOf<CandyType>()

                // Prevent horizontal match: check two to the left
                if (col >= 2) {
                    val left1 = cells[row][col - 1].candy?.type
                    val left2 = cells[row][col - 2].candy?.type
                    if (left1 != null && left1 == left2) forbidden.add(left1)
                }

                // Prevent vertical match: check two above
                if (row >= 2) {
                    val up1 = cells[row - 1][col].candy?.type
                    val up2 = cells[row - 2][col].candy?.type
                    if (up1 != null && up1 == up2) forbidden.add(up1)
                }

                val allowed = CandyType.entries.filter { it !in forbidden }
                val type = if (allowed.isNotEmpty()) allowed.random() else CandyType.entries.random()
                cells[row][col] = Cell(row = row, col = col, candy = Candy(id = idCounter(), type = type))
            }
        }

        return Board(
            cells = cells.map { row -> row.toList() },
            size = size,
        )
    }
}
