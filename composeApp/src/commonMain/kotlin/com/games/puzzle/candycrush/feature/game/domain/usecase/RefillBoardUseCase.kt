package com.games.puzzle.candycrush.feature.game.domain.usecase

import com.games.puzzle.candycrush.feature.game.domain.model.Board
import com.games.puzzle.candycrush.feature.game.domain.model.Candy
import com.games.puzzle.candycrush.feature.game.domain.model.CandyType

class RefillBoardUseCase {
    operator fun invoke(board: Board, idCounter: () -> Long): Board {
        var result = board
        for (row in 0 until board.size) {
            for (col in 0 until board.size) {
                if (result.getCell(row, col).isEmpty) {
                    result = result.withCell(row, col, Candy(id = idCounter(), type = CandyType.random()))
                }
            }
        }
        return result
    }
}
