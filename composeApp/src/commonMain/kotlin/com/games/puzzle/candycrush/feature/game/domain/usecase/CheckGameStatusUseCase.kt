package com.games.puzzle.candycrush.feature.game.domain.usecase

import com.games.puzzle.candycrush.feature.game.domain.model.GameState
import com.games.puzzle.candycrush.feature.game.domain.model.GameStatus

class CheckGameStatusUseCase {
    operator fun invoke(state: GameState): GameStatus = when {
        state.score >= state.config.targetScore -> GameStatus.Won
        state.movesRemaining <= 0 -> GameStatus.Lost
        else -> GameStatus.Playing
    }
}
