package com.games.puzzle.candycrush.feature.game.domain.repository

import com.games.puzzle.candycrush.feature.game.domain.model.GameConfig
import com.games.puzzle.candycrush.feature.game.domain.model.GameState

interface GameRepository {
    fun createGame(config: GameConfig): GameState
    fun processSelection(state: GameState, row: Int, col: Int): GameState
    fun restartGame(state: GameState): GameState
}
