package com.games.puzzle.candycrush.feature.game.domain.repository

import com.games.puzzle.candycrush.feature.game.domain.model.GameConfig
import com.games.puzzle.candycrush.feature.game.domain.model.GameState
import com.games.puzzle.candycrush.feature.game.domain.model.TurnResult

interface GameRepository {
    fun createGame(config: GameConfig): GameState
    fun processSelection(state: GameState, row: Int, col: Int): GameState
    fun processSwipe(state: GameState, fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): TurnResult
    fun restartGame(state: GameState): GameState
}
