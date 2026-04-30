package com.games.puzzle.candycrush.feature.game.domain.usecase

import com.games.puzzle.candycrush.feature.game.domain.model.GameState

class SelectCandyUseCase {
    operator fun invoke(state: GameState, row: Int, col: Int): GameState {
        val cell = state.board.getCell(row, col)
        if (cell.isEmpty) return state.copy(selectedCell = null)

        val selected = state.selectedCell
        return when {
            selected == null -> state.copy(selectedCell = row to col)
            selected.first == row && selected.second == col -> state.copy(selectedCell = null)
            else -> state.copy(selectedCell = row to col)
        }
    }
}
