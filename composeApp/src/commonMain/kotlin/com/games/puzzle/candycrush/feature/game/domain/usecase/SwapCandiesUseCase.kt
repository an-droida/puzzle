package com.games.puzzle.candycrush.feature.game.domain.usecase

import com.games.puzzle.candycrush.feature.game.domain.model.Board

class SwapCandiesUseCase {
    operator fun invoke(board: Board, row1: Int, col1: Int, row2: Int, col2: Int): Board {
        val candy1 = board.getCell(row1, col1).candy
        val candy2 = board.getCell(row2, col2).candy
        return board
            .withCell(row1, col1, candy2)
            .withCell(row2, col2, candy1)
    }
}
